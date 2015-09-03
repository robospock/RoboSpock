package pl.polidea.robospock.internal;

import org.jetbrains.annotations.NotNull;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.*;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.bytecode.AsmInstrumentingClassLoader;
import org.robolectric.bytecode.Setup;
import org.robolectric.bytecode.ShadowMap;
import org.robolectric.res.DocumentLoader;
import org.robolectric.internal.dependency.CachedDependencyResolver;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.internal.dependency.LocalDependencyResolver;
import org.robolectric.internal.dependency.MavenDependencyResolver;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResourceLoader;
import org.robolectric.util.Logger;
import org.spockframework.runtime.Sputnik;
import org.spockframework.runtime.model.SpecInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.security.SecureRandom;
import java.util.*;

public class RoboSputnik extends Runner implements Filterable, Sortable {
    // Robolectric
    // private static final String CONFIG_PROPERTIES = "robolectric.properties";
    private static final Config DEFAULT_CONFIG = new Config.Implementation(defaultsFor(Config.class));
    private InstrumentingClassLoaderFactory instrumentingClassLoaderFactory;
    private DependencyResolver dependencyResolver;

    protected DependencyResolver getJarResolver() {
        if (dependencyResolver == null) {
            if (Boolean.getBoolean("robolectric.offline")) {
                String dependencyDir = System.getProperty("robolectric.dependency.dir", ".");
                dependencyResolver = new LocalDependencyResolver(new File(dependencyDir));
            } else {
                File cacheDir = new File(new File(System.getProperty("java.io.tmpdir")), "robolectric");
                cacheDir.mkdir();

                if (cacheDir.exists()) {
                    Logger.info("Dependency cache location: %s", cacheDir.getAbsolutePath());
                    dependencyResolver = new CachedDependencyResolver(new MavenDependencyResolver(), cacheDir, 60 * 60 * 24 * 1000);
                } else {
                    dependencyResolver = new MavenDependencyResolver();
                }
            }
        }

        return dependencyResolver;
    }

    private Object sputnik;

    static {
        new SecureRandom(); // this starts up the Poller SunPKCS11-Darwin thread early, outside of any Robolectric classloader
    }

    public RoboSputnik(Class<?> clazz) throws InitializationError {

        // Ripped from RobolectricTestRunner

        final Config config = getConfig(clazz);
        AndroidManifest appManifest = getAppManifest(config);
        if (instrumentingClassLoaderFactory == null) {
            instrumentingClassLoaderFactory = new InstrumentingClassLoaderFactory(createClassLoaderConfig(), getJarResolver());
        }
        SdkEnvironment sdkEnvironment = instrumentingClassLoaderFactory.getSdkEnvironment(new SdkConfig(pickSdkVersion(config, appManifest)));

        // todo: is this really needed?
        Thread.currentThread().setContextClassLoader(sdkEnvironment.getRobolectricClassLoader());

        Class bootstrappedTestClass = sdkEnvironment.bootstrappedClass(clazz);

        // Since we have bootstrappedClass we may properly initialize

        try {

            this.sputnik = sdkEnvironment
                    .bootstrappedClass(Sputnik.class)
                    .getConstructor(Class.class)
                    .newInstance(bootstrappedTestClass);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // let's manually add our initializers

        for(Method method : sputnik.getClass().getDeclaredMethods()) {
            if(method.getName().equals("getSpec")) {
                method.setAccessible(true);
                try {
                    Object spec = method.invoke(sputnik);

                    // Interceptor registers on construction
                    sdkEnvironment
                            .bootstrappedClass(RoboSpockInterceptor.class)
                            .getConstructor(
                                    sdkEnvironment.bootstrappedClass(SpecInfo.class),
                                    SdkEnvironment.class,
                                    Config.class,
                                    AndroidManifest.class
                            ).newInstance(spec, sdkEnvironment, config, appManifest);

                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * NOTE: originally in RobolectricTestRunner getConfig takes Method as parameter
     * and is a bit more complicated
     */
    public Config getConfig(Class<?> clazz) {
        Config config = DEFAULT_CONFIG;

        Config globalConfig = Config.Implementation.fromProperties(getConfigProperties());
        if (globalConfig != null) {
            config = new Config.Implementation(config, globalConfig);
        }

        Config classConfig = clazz.getAnnotation(Config.class);
        if (classConfig != null) {
            config = new Config.Implementation(config, classConfig);
        }

        return config;
    }

    protected Properties getConfigProperties() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("org.robolectric.Config.properties");
        if (resourceAsStream == null) return null;
        Properties properties = new Properties();
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    protected AndroidManifest getAppManifest(Config config) {
        if (config.manifest().equals(Config.NONE)) {
            return null;
        }

        boolean propertyAvailable = false;
        FsFile manifestFile = null;
        String manifestProperty = System.getProperty("android.manifest");
        if (config.manifest().equals(Config.DEFAULT) && manifestProperty != null) {
            manifestFile = Fs.fileFromPath(manifestProperty);
            propertyAvailable = true;
        } else {
            FsFile fsFile = Fs.currentDirectory();
            String manifestStr = config.manifest().equals(Config.DEFAULT) ? "AndroidManifest.xml" : config.manifest();
            manifestFile = fsFile.join(manifestStr);
        }

        synchronized (envHolder) {
            AndroidManifest appManifest;
            appManifest = envHolder.appManifestsByFile.get(manifestFile);
            if (appManifest == null) {

                long startTime = System.currentTimeMillis();
                appManifest = propertyAvailable ? createAppManifestFromProperty(manifestFile) : createAppManifest(manifestFile);
                if (DocumentLoader.DEBUG_PERF)
                    System.out.println(String.format("%4dms spent in %s", System.currentTimeMillis() - startTime, manifestFile));

                envHolder.appManifestsByFile.put(manifestFile, appManifest);
            }
            return appManifest;
        }
    }

    protected AndroidManifest createAppManifest(FsFile manifestFile) {
        if (!manifestFile.exists()) {
            System.out.print("WARNING: No manifest file found at " + manifestFile.getPath() + ".");
            System.out.println("Falling back to the Android OS resources only.");
            System.out.println("To remove this warning, annotate your test class with @Config(manifest=Config.NONE).");
            return null;
        }

        FsFile appBaseDir = manifestFile.getParent();
        return new AndroidManifest(manifestFile, appBaseDir.join("res"), appBaseDir.join("assets"));
    }

    protected AndroidManifest createAppManifestFromProperty(FsFile manifestFile) {
        String resProperty = System.getProperty("android.resources");
        String assetsProperty = System.getProperty("android.assets");
        AndroidManifest manifest = new AndroidManifest(manifestFile, Fs.fileFromPath(resProperty), Fs.fileFromPath(assetsProperty));
        String packageProperty = System.getProperty("android.package");

        if (packageProperty != null) {
            try {
                setPackageName(manifest, packageProperty);
            } catch (IllegalArgumentException e) {
                System.out.println("WARNING: Faild to set package name for " + manifestFile.getPath() + ".");
            }
        }
        return manifest;
    }


    private void setPackageName(AndroidManifest manifest, String packageName) {
        Class<AndroidManifest> type = AndroidManifest.class;
        try {
            Method setPackageNameMethod = type.getMethod("setPackageName", String.class);
            setPackageNameMethod.setAccessible(true);
            setPackageNameMethod.invoke(manifest, packageName);
            return;
        } catch (NoSuchMethodException e) {
            try {

                //Force execute parseAndroidManifest.
                manifest.getPackageName();

                Field packageNameField = type.getDeclaredField("packageName");
                packageNameField.setAccessible(true);
                packageNameField.set(manifest, packageName);
                return;
            } catch (Exception fieldError) {
                throw new IllegalArgumentException(fieldError);
            }
        } catch (Exception methodError) {
            throw new IllegalArgumentException(methodError);
        }
    }


    public Setup createSetup() {
        return new Setup() {
            @Override
            public boolean shouldAcquire(String name) {

                List<String> prefixes = Arrays.asList(
                        DependencyResolver.class.getName(),
                        "org.junit",
                        ShadowMap.class.getName()
                );

                if(name != null) {
                    for(String prefix : prefixes) {
                        if (name.startsWith(prefix)) {
                            return false;
                        }
                    }
                }

                return super.shouldAcquire(name);
            }
        };
    }

//    protected void setUpApplicationState(Method method, ParallelUniverseInterface parallelUniverseInterface, ResourceLoader systemResourceLoader, AndroidManifest appManifest, Config config) {

    protected int pickSdkVersion(Config config, AndroidManifest manifest) {
        if (config != null && config.sdk().length > 1) {
            throw new IllegalArgumentException("Robospock does not support multiple values for @Config.sdk");
        } else if (config != null && config.sdk().length == 1) {
            return config.sdk()[0];
        } else if (manifest != null) {
            return manifest.getTargetSdkVersion();
        } else {
            return SdkConfig.FALLBACK_SDK_VERSION;
        }
    }

    public Description getDescription() {
        return ((Runner) sputnik).getDescription();
    }

    public void run(RunNotifier notifier) {
        ((Runner) sputnik).run(notifier);
    }

    public void filter(Filter filter) throws NoTestsRemainException {
        ((Filterable) sputnik).filter(filter);
    }

    public void sort(Sorter sorter) {
        ((Sortable) sputnik).sort(sorter);
    }

//    private ParallelUniverseInterface getHooksInterface(SdkEnvironment sdkEnvironment) {

//    public void internalAfterTest(final Method method) {

//    private void afterClass() {

//    @TestOnly
//    boolean allStateIsCleared() {

//    @Override
//    public Object createTest() throws Exception {

//    public final ResourceLoader getAppResourceLoader(SdkConfig sdkConfig, ResourceLoader systemResourceLoader, final AndroidManifest appManifest) {

//    protected ResourceLoader createAppResourceLoader(ResourceLoader systemResourceLoader, AndroidManifest appManifest) {

//    public PackageResourceLoader createResourceLoader(ResourcePath resourcePath) {

    protected ShadowMap createShadowMap() {
        synchronized (RoboSputnik.class) {
            if (mainShadowMap != null) return mainShadowMap;
            mainShadowMap = new ShadowMap.Builder().build();
            return mainShadowMap;
        }
    }

//    public class HelperTestRunner extends BlockJUnit4ClassRunner {

    private static class ManifestIdentifier {
        private final FsFile manifestFile;
        private final FsFile resDir;
        private final FsFile assetDir;
        private final String packageName;
        private final List<FsFile> libraryDirs;

        public ManifestIdentifier(FsFile manifestFile, FsFile resDir, FsFile assetDir, String packageName,
                                  List<FsFile> libraryDirs) {
            this.manifestFile = manifestFile;
            this.resDir = resDir;
            this.assetDir = assetDir;
            this.packageName = packageName;
            this.libraryDirs = libraryDirs != null ? libraryDirs : Collections.<FsFile>emptyList();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ManifestIdentifier that = (ManifestIdentifier) o;

            return assetDir.equals(that.assetDir)
                    && libraryDirs.equals(that.libraryDirs)
                    && manifestFile.equals(that.manifestFile)
                    && resDir.equals(that.resDir)
                    && ((packageName == null && that.packageName == null) || (packageName != null && packageName.equals(that.packageName)));
        }

        @Override
        public int hashCode() {
            int result = manifestFile.hashCode();
            result = 31 * result + resDir.hashCode();
            result = 31 * result + assetDir.hashCode();
            result = 31 * result + (packageName == null ? 0 : packageName.hashCode());
            result = 31 * result + libraryDirs.hashCode();
            return result;
        }
    }

    private static <A extends Annotation> A defaultsFor(Class<A> annotation) {
        return annotation.cast(
                Proxy.newProxyInstance(annotation.getClassLoader(), new Class[]{annotation},
                        new InvocationHandler() {
                            public Object invoke(Object proxy, @NotNull Method method, Object[] args)
                                    throws Throwable {
                                return method.getDefaultValue();
                            }
                        }));
    }
}
