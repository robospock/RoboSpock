package pl.polidea.robospock.internal;

import android.app.Application;
import android.os.Build;

import org.robolectric.DefaultTestLifecycle;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycle;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.SdkEnvironment;
import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.RobolectricInternals;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.internal.bytecode.ShadowWrangler;
import org.robolectric.internal.dependency.CachedDependencyResolver;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.internal.dependency.LocalDependencyResolver;
import org.robolectric.internal.dependency.MavenDependencyResolver;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.ResourceLoader;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.SpecInfo;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RoboSpockInterceptor extends AbstractMethodInterceptor {
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

    private static ShadowMap mainShadowMap;
    private TestLifecycle<Application> testLifecycle;

    private SpecInfo specInfo;
    private final SdkEnvironment sdkEnvironment;
    private final Config config;
    private final AndroidManifest appManifest;

    public RoboSpockInterceptor(
            SpecInfo specInfo, SdkEnvironment sdkEnvironment, Config config, AndroidManifest appManifest) {

        this.sdkEnvironment = sdkEnvironment;
        this.config = config;
        this.appManifest = appManifest;
        this.specInfo = specInfo;

        this.specInfo.addInterceptor(this);
    }

    @Override
    public void interceptSpecExecution(IMethodInvocation invocation) throws Throwable {

        configureShadows(sdkEnvironment, config);

        ParallelUniverseInterface parallelUniverseInterface = getHooksInterface(sdkEnvironment);
        try {
            assureTestLifecycle(sdkEnvironment);

            parallelUniverseInterface.resetStaticState(config);
            parallelUniverseInterface.setSdkConfig(sdkEnvironment.getSdkConfig());

            int sdkVersion = pickSdkVersion(config, appManifest);
            Class<?> versionClass = sdkEnvironment.bootstrappedClass(Build.VERSION.class);
            ReflectionHelpers.setStaticField(versionClass, "SDK_INT", sdkVersion);

            ResourceLoader systemResourceLoader = sdkEnvironment.getSystemResourceLoader(getJarResolver());
            setUpApplicationState(null, parallelUniverseInterface, systemResourceLoader, appManifest, config);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try {
            invocation.proceed();
        } finally {
            parallelUniverseInterface.resetStaticState(config);
        }
    }

    protected void setUpApplicationState(Method method, ParallelUniverseInterface parallelUniverseInterface, ResourceLoader systemResourceLoader, AndroidManifest appManifest, Config config) {
        parallelUniverseInterface.setUpApplicationState(method, testLifecycle, systemResourceLoader, appManifest, config);
    }

    protected int pickSdkVersion(Config config, AndroidManifest manifest) {
        if (config != null && config.sdk().length > 1) {
            throw new IllegalArgumentException("RoboSpock does not support multiple values for @Config.sdk");
        } else if (config != null && config.sdk().length == 1) {
            return config.sdk()[0];
        } else if (manifest != null) {
            return manifest.getTargetSdkVersion();
        } else {
            return SdkConfig.FALLBACK_SDK_VERSION;
        }
    }

    private ParallelUniverseInterface getHooksInterface(SdkEnvironment sdkEnvironment) {
        try {
            @SuppressWarnings("unchecked")
            Class<ParallelUniverseInterface> aClass = (Class<ParallelUniverseInterface>)
                    sdkEnvironment.getRobolectricClassLoader().loadClass(ParallelUniverseCompat.class.getName());

            return aClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void assureTestLifecycle(SdkEnvironment sdkEnvironment) {
        try {
            ClassLoader robolectricClassLoader = sdkEnvironment.getRobolectricClassLoader();
            testLifecycle = (TestLifecycle) robolectricClassLoader.loadClass(getTestLifecycleClass().getName()).newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected Class<? extends TestLifecycle> getTestLifecycleClass() {
        return DefaultTestLifecycle.class;
    }

    protected void configureShadows(SdkEnvironment sdkEnvironment, Config config) {
        ShadowMap shadowMap = createShadowMap();

        if (config != null) {
            Class<?>[] shadows = config.shadows();
            if (shadows.length > 0) {
                shadowMap = shadowMap.newBuilder()
                        .addShadowClasses(shadows)
                        .build();
            }
        }

        ClassHandler classHandler = getClassHandler(sdkEnvironment, shadowMap);
        injectClassHandler(sdkEnvironment.getRobolectricClassLoader(), classHandler);
    }

    public static void injectClassHandler(ClassLoader robolectricClassLoader, ClassHandler classHandler) {
        try {
            String className = RobolectricInternals.class.getName();
            Class<?> robolectricInternalsClass = robolectricClassLoader.loadClass(className);
            Field field = robolectricInternalsClass.getDeclaredField("classHandler");
            field.setAccessible(true);
            field.set(null, classHandler);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected ShadowMap createShadowMap() {
        synchronized (RobolectricTestRunner.class) {
            if (mainShadowMap != null) return mainShadowMap;

            mainShadowMap = new ShadowMap.Builder().build();
            return mainShadowMap;
        }
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

    protected ClassHandler createClassHandler(ShadowMap shadowMap, SdkConfig sdkConfig) {
        return new ShadowWrangler(shadowMap);
    }
}
