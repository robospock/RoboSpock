package pl.polidea.robospock.internal;

import org.jetbrains.annotations.NotNull;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.internal.InstrumentingClassLoaderFactory;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.SdkEnvironment;
import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.RobolectricInternals;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.internal.bytecode.ShadowWrangler;
import org.robolectric.internal.dependency.CachedDependencyResolver;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.internal.dependency.LocalDependencyResolver;
import org.robolectric.internal.dependency.MavenDependencyResolver;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;
import org.spockframework.runtime.Sputnik;
import org.spockframework.runtime.model.SpecInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class RoboSputnik extends Runner implements Filterable, Sortable {
    private static final String CONFIG_PROPERTIES = "robolectric.properties";
    private static final Config DEFAULT_CONFIG = new Config.Implementation(defaultsFor(Config.class));
    //    private static final Map<Pair<AndroidManifest, SdkConfig>, ResourceLoader> resourceLoadersByManifestAndConfig = new HashMap<>();
    private static final Map<ManifestIdentifier, AndroidManifest> appManifestsByFile = new HashMap<>();
    private static ShadowMap mainShadowMap;
    private InstrumentingClassLoaderFactory instrumentingClassLoaderFactory;
    //    private TestLifecycle<Application> testLifecycle;
    private DependencyResolver dependencyResolver;

    private Object sputnik;

    static {
        new SecureRandom(); // this starts up the Poller SunPKCS11-Darwin thread early, outside of any Robolectric classloader
    }

    public RoboSputnik(Class<?> clazz) throws InitializationError {

        // Ripped from RobolectricTestRunner::runChild()

        final Config config = getConfig(clazz);
        AndroidManifest appManifest = getAppManifest(config);
        if (instrumentingClassLoaderFactory == null) {
            instrumentingClassLoaderFactory = new InstrumentingClassLoaderFactory(createClassLoaderConfig(), getJarResolver());
        }
        SdkEnvironment sdkEnvironment = instrumentingClassLoaderFactory.getSdkEnvironment(new SdkConfig(pickSdkVersion(config, appManifest)));

        // RobolectricTestRunner::methodBlock()

        configureShadows(sdkEnvironment, config);

        Class bootstrappedTestClass = sdkEnvironment.bootstrappedClass(clazz);
//        RobolectricTestRunner.HelperTestRunner helperTestRunner = getHelperTestRunner(bootstrappedTestClass);

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

//    public RobolectricTestRunner(final Class<?> testClass) throws InitializationError {

//    @SuppressWarnings("unchecked")
//    private void assureTestLifecycle(SdkEnvironment sdkEnvironment) {

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

    protected ClassHandler createClassHandler(ShadowMap shadowMap, SdkConfig sdkConfig) {
        return new ShadowWrangler(shadowMap);
    }

    protected AndroidManifest createAppManifest(FsFile manifestFile, FsFile resDir, FsFile assetDir, String packageName) {
        if (!manifestFile.exists()) {
            System.out.print("WARNING: No manifest file found at " + manifestFile.getPath() + ".");
            System.out.println("Falling back to the Android OS resources only.");
            System.out.println("To remove this warning, annotate your test class with @Config(manifest=Config.NONE).");
            return null;
        }

        Logger.debug("Robolectric assets directory: " + assetDir.getPath());
        Logger.debug("   Robolectric res directory: " + resDir.getPath());
        Logger.debug("   Robolectric manifest path: " + manifestFile.getPath());
        Logger.debug("    Robolectric package name: " + packageName);
        return new AndroidManifest(manifestFile, resDir, assetDir, packageName);
    }

    public InstrumentationConfiguration createClassLoaderConfig() {
        return InstrumentationConfiguration.newBuilder()
//                .doNotAquireClass(ShadowMap.class.getName())
                .doNotAquireClass(DependencyResolver.class.getName())
                .build();
    }

//    protected Class<? extends TestLifecycle> getTestLifecycleClass() {

    public static void injectClassHandler(ClassLoader robolectricClassLoader, ClassHandler classHandler) {
        String className = RobolectricInternals.class.getName();
        Class<?> robolectricInternalsClass = ReflectionHelpers.loadClass(robolectricClassLoader, className);
        ReflectionHelpers.setStaticField(robolectricInternalsClass, "classHandler", classHandler);
    }

//    @Override
//    protected Statement classBlock(RunNotifier notifier) {

//    private void invokeAfterClass(final Class<?> clazz) throws Throwable {

//    @Override
//    protected void runChild(FrameworkMethod method, RunNotifier notifier) {

//    protected boolean shouldIgnore(FrameworkMethod method, Config config) {

//    private Statement methodBlock(final FrameworkMethod method, final Config config, final AndroidManifest appManifest, final SdkEnvironment sdkEnvironment) {

//    private void invokeBeforeClass(final Class clazz) throws Throwable {

//    protected HelperTestRunner getHelperTestRunner(Class bootstrappedTestClass) {

    protected AndroidManifest getAppManifest(Config config) {
        if (config.manifest().equals(Config.NONE)) {
            return null;
        }

        String manifestProperty = System.getProperty("android.manifest");
        String resourcesProperty = System.getProperty("android.resources");
        String assetsProperty = System.getProperty("android.assets");
        String packageName = System.getProperty("android.package");

        FsFile baseDir;
        FsFile manifestFile;
        FsFile resDir;
        FsFile assetDir;

        boolean defaultManifest = config.manifest().equals(Config.DEFAULT);
        if (defaultManifest && manifestProperty != null) {
            manifestFile = Fs.fileFromPath(manifestProperty);
            baseDir = manifestFile.getParent();
        } else {
            manifestFile = getBaseDir().join(defaultManifest ? AndroidManifest.DEFAULT_MANIFEST_NAME : config.manifest());
            baseDir = manifestFile.getParent();
        }

        boolean defaultRes = Config.DEFAULT_RES_FOLDER.equals(config.resourceDir());
        if (defaultRes && resourcesProperty != null) {
            resDir = Fs.fileFromPath(resourcesProperty);
        } else {
            resDir = baseDir.join(config.resourceDir());
        }

        boolean defaultAssets = Config.DEFAULT_ASSET_FOLDER.equals(config.assetDir());
        if (defaultAssets && assetsProperty != null) {
            assetDir = Fs.fileFromPath(assetsProperty);
        } else {
            assetDir = baseDir.join(config.assetDir());
        }

        String configPackageName = config.packageName();
        if (configPackageName != null && !configPackageName.isEmpty()) {
            packageName = configPackageName;
        }

        List<FsFile> libraryDirs = null;
        if (config.libraries().length > 0) {
            libraryDirs = new ArrayList<>();
            for (String libraryDirName : config.libraries()) {
                libraryDirs.add(baseDir.join(libraryDirName));
            }
        }

        ManifestIdentifier identifier = new ManifestIdentifier(manifestFile, resDir, assetDir, packageName, libraryDirs);
        synchronized (appManifestsByFile) {
            AndroidManifest appManifest;
            appManifest = appManifestsByFile.get(identifier);
            if (appManifest == null) {
                appManifest = createAppManifest(manifestFile, resDir, assetDir, packageName);
                if (libraryDirs != null) {
                    appManifest.setLibraryDirectories(libraryDirs);
                }
                appManifestsByFile.put(identifier, appManifest);
            }
            return appManifest;
        }

    }

    protected FsFile getBaseDir() {
        return Fs.currentDirectory();
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
        try (InputStream resourceAsStream = classLoader.getResourceAsStream(CONFIG_PROPERTIES)) {
            if (resourceAsStream == null) return null;
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void configureShadows(SdkEnvironment sdkEnvironment, Config config) {
        ShadowMap shadowMap = createShadowMap();

        if (config != null) {
            Class<?>[] shadows = config.shadows();
            if (shadows.length > 0) {
                shadowMap = shadowMap.newBuilder().addShadowClasses(shadows).build();
            }
        }

        ClassHandler classHandler = getClassHandler(sdkEnvironment, shadowMap);
        injectClassHandler(sdkEnvironment.getRobolectricClassLoader(), classHandler);
    }

    private ClassHandler getClassHandler(SdkEnvironment sdkEnvironment, ShadowMap shadowMap) {
        ClassHandler classHandler;
        synchronized (sdkEnvironment) {
            classHandler = sdkEnvironment.classHandlersByShadowMap.get(shadowMap);
            if (classHandler == null) {
                classHandler = createClassHandler(shadowMap, sdkEnvironment.getSdkConfig());
            }
        }
        return classHandler;
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