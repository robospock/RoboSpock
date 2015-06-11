package pl.polidea.robospock.internal;

import android.app.Application;
import android.os.Build;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.bytecode.ClassHandler;
import org.robolectric.bytecode.RobolectricInternals;
import org.robolectric.bytecode.ShadowMap;
import org.robolectric.bytecode.ShadowWrangler;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.ReflectionHelpers;
import org.robolectric.res.ResourceLoader;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.SpecInfo;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RoboSpockInterceptor extends AbstractMethodInterceptor {
    private DependencyResolver dependencyResolver;

    protected DependencyResolver getJarResolver() {
        if (dependencyResolver == null) {
            if (Boolean.getBoolean("robolectric.offline")) {
                String dependencyDir = System.getProperty("robolectric.dependency.dir", ".");
                dependencyResolver = new LocalDependencyResolver(new File(dependencyDir));
            } else {
                dependencyResolver = new MavenDependencyResolver();
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

            parallelUniverseInterface.resetStaticState();
            parallelUniverseInterface.setSdkConfig(sdkEnvironment.getSdkConfig());

            boolean strictI18n = determineI18nStrictState();

            int sdkVersion = pickReportedSdkVersion(config, appManifest);
            Class<?> versionClass = sdkEnvironment.bootstrappedClass(Build.VERSION.class);
            ReflectionHelpers.setStaticFieldReflectively(versionClass, "SDK_INT", sdkVersion);

            ResourceLoader systemResourceLoader = sdkEnvironment.getSystemResourceLoader(getJarResolver(), null);
            setUpApplicationState(null, parallelUniverseInterface, strictI18n, systemResourceLoader, appManifest, config);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }


        Map<Field, Object> withConstantAnnos = getWithConstantAnnotations();

        setupConstants(withConstantAnnos);

        try {
            invocation.proceed();
        } finally {
            parallelUniverseInterface.resetStaticState();
        }

    }

    private void setupConstants(Map<Field, Object> constants) {
        for (Field field : constants.keySet()) {
            Object newValue = constants.get(field);
            Object oldValue = Robolectric.Reflection.setFinalStaticField(field, newValue);
            constants.put(field, oldValue);
        }
    }

    private Map<Field, Object> getWithConstantAnnotations() {
        Map<Field, Object> constants = new HashMap<Field, Object>();

        for (Annotation anno : specInfo.getReflection().getAnnotations()) {
            addConstantFromAnnotation(constants, anno);
        }

        return constants;
    }

    private void addConstantFromAnnotation(Map<Field, Object> constants, Annotation anno) {
        try {
            String name = anno.annotationType().getName();
            Object newValue = null;

            if (name.equals(WithConstantString.class.getName())) {
                newValue = anno.annotationType().getMethod("newValue").invoke(anno);
            } else if (name.equals(WithConstantInt.class.getName())) {
                newValue = anno.annotationType().getMethod("newValue").invoke(anno);
            } else {
                return;
            }

            @SuppressWarnings("rawtypes")
            Class classWithField = (Class) anno.annotationType().getMethod("classWithField").invoke(anno);
            String fieldName = (String) anno.annotationType().getMethod("fieldName").invoke(anno);
            Field field = classWithField.getDeclaredField(fieldName);
            constants.put(field, newValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void setUpApplicationState(Method method, ParallelUniverseInterface parallelUniverseInterface, boolean strictI18n, ResourceLoader systemResourceLoader, AndroidManifest appManifest, Config config) {
        parallelUniverseInterface.setUpApplicationState(method, testLifecycle, strictI18n, systemResourceLoader, appManifest, config);
    }

    protected int pickReportedSdkVersion(Config config, AndroidManifest appManifest) {
        if (config != null && config.reportSdk() != -1) {
            return config.reportSdk();
        } else {
            return getTargetSdkVersion(appManifest);
        }
    }

    private int getTargetSdkVersion(AndroidManifest appManifest) {
        return getTargetVersionWhenAppManifestMightBeNullWhaaa(appManifest);
    }

    public static int getTargetVersionWhenAppManifestMightBeNullWhaaa(AndroidManifest appManifest) {
        return appManifest == null // app manifest would be null for libraries
                ? Build.VERSION_CODES.ICE_CREAM_SANDWICH // todo: how should we be picking this?
                : appManifest.getTargetSdkVersion();
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
            sdkEnvironment.setCurrentClassHandler(classHandler);
        }
        return classHandler;
    }

    protected ClassHandler createClassHandler(ShadowMap shadowMap, SdkConfig sdkConfig) {
        return new ShadowWrangler(shadowMap, sdkConfig);
    }

    private boolean determineI18nStrictState() {
        // Global
        boolean strictI18n = globalI18nStrictEnabled();

        // Test case class
        Class<?> testClass = specInfo.getReflection();
        if (testClass.getAnnotation(EnableStrictI18n.class) != null) {
            strictI18n = true;
        } else if (testClass.getAnnotation(DisableStrictI18n.class) != null) {
            strictI18n = false;
        }

        return strictI18n;
    }

    private boolean globalI18nStrictEnabled() {
        return Boolean.valueOf(System.getProperty("robolectric.strictI18n"));
    }
}
