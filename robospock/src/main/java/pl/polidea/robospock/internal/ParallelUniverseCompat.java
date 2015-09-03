package pl.polidea.robospock.internal;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import org.robolectric.*;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.ReflectionHelpers;
import org.robolectric.res.*;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.res.ResBundle;
import org.robolectric.shadows.ShadowActivityThread;
import org.robolectric.shadows.ShadowContextImpl;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowResources;
import org.robolectric.util.Pair;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.internal.ReflectionHelpers.ClassParameter;

public class ParallelUniverseCompat implements ParallelUniverseInterface {
    private static final String DEFAULT_PACKAGE_NAME = "org.robolectric.default";
    // robolectric
    // private final RobolectricTestRunner robolectricTestRunner;
    private static Map<Pair<AndroidManifest, SdkConfig>, ResourceLoader> resourceLoadersByManifestAndConfig = new HashMap<Pair<AndroidManifest, SdkConfig>, ResourceLoader>();

    private boolean loggingInitialized = false;
    private SdkConfig sdkConfig;

    // robolectric
    // public ParallelUniverse(RobolectricTestRunner robolectricTestRunner) {
    //     this.robolectricTestRunner = robolectricTestRunner;
    // }

    @Override
    public void resetStaticState(Config config) {
        Robolectric.reset();

        if (!loggingInitialized) {
            ShadowLog.setupLogging();
            loggingInitialized = true;
        }
    }

    /*
     * If the Config already has a version qualifier, do nothing. Otherwise, add a version
     * qualifier for the target api level (which comes from the manifest or Config.emulateSdk()).
     */
    private String addVersionQualifierToQualifiers(String qualifiers) {
        int versionQualifierApiLevel = ResBundle.getVersionQualifierApiLevel(qualifiers);
        if (versionQualifierApiLevel == -1) {
            if (qualifiers.length() > 0) {
                qualifiers += "-";
            }
            qualifiers += "v" + sdkConfig.getApiLevel();
        }
        return qualifiers;
    }

    @Override
    public void setUpApplicationState(Method method, TestLifecycle testLifecycle, ResourceLoader systemResourceLoader, AndroidManifest appManifest, Config config) {
        Robolectric.packageManager = new RobolectricPackageManager();
        Robolectric.packageManager.addPackage(DEFAULT_PACKAGE_NAME);
        RuntimeEnvironment.application = null;
        ResourceLoader resourceLoader;
        if (appManifest != null) {
            // robolectric
            // resourceLoader = robolectricTestRunner.getAppResourceLoader(sdkConfig, systemResourceLoader, appManifest);
            resourceLoader = getAppResourceLoader(sdkConfig, systemResourceLoader, appManifest);
            Robolectric.packageManager.addManifest(appManifest, resourceLoader);
        } else {
            resourceLoader = systemResourceLoader;
        }

        ShadowResources.setSystemResources(systemResourceLoader);
        String qualifiers = addVersionQualifierToQualifiers(config.qualifiers());
        Resources systemResources = Resources.getSystem();
        Configuration configuration = systemResources.getConfiguration();
        shadowOf(configuration).overrideQualifiers(qualifiers);
        systemResources.updateConfiguration(configuration, systemResources.getDisplayMetrics());
        shadowOf(systemResources.getAssets()).setQualifiers(qualifiers);

        Class<?> contextImplClass = ReflectionHelpers.loadClassReflectively(getClass().getClassLoader(), ShadowContextImpl.CLASS_NAME);

        Class<?> activityThreadClass = ReflectionHelpers.loadClassReflectively(getClass().getClassLoader(), ShadowActivityThread.CLASS_NAME);
        Object activityThread = ReflectionHelpers.callConstructorReflectively(activityThreadClass);
        Robolectric.activityThread = activityThread;

        ReflectionHelpers.setFieldReflectively(activityThread, "mInstrumentation", new RoboInstrumentation());
        ReflectionHelpers.setFieldReflectively(activityThread, "mCompatConfiguration", configuration);

        Context systemContextImpl = ReflectionHelpers.callStaticMethodReflectively(contextImplClass, "createSystemContext", new ReflectionHelpers.ClassParameter(activityThreadClass, activityThread));

        final Application application = (Application) testLifecycle.createApplication(method, appManifest, config);
        if (application != null) {
            String packageName = appManifest != null ? appManifest.getPackageName() : null;
            if (packageName == null) packageName = DEFAULT_PACKAGE_NAME;

            ApplicationInfo applicationInfo;
            try {
                applicationInfo = Robolectric.packageManager.getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }

            Class<?> compatibilityInfoClass = ReflectionHelpers.loadClassReflectively(getClass().getClassLoader(), "android.content.res.CompatibilityInfo");

            Object loadedApk = ReflectionHelpers.callInstanceMethodReflectively(activityThread, "getPackageInfo", new ClassParameter(ApplicationInfo.class, applicationInfo),
                    new ClassParameter(compatibilityInfoClass, null), new ClassParameter(ClassLoader.class, getClass().getClassLoader()), new ClassParameter(boolean.class, false),
                    new ClassParameter(boolean.class, true)
            );

            shadowOf(application).bind(appManifest, resourceLoader);
            if (appManifest == null) {
                // todo: make this cleaner...
                shadowOf(application).setPackageName(applicationInfo.packageName);
            }
            Resources appResources = application.getResources();
            ReflectionHelpers.setFieldReflectively(loadedApk, "mResources", appResources);
            Context contextImpl = ReflectionHelpers.callInstanceMethodReflectively(systemContextImpl, "createPackageContext", new ClassParameter(String.class, applicationInfo.packageName), new ClassParameter(int.class, Context.CONTEXT_INCLUDE_CODE));
            ReflectionHelpers.setFieldReflectively(activityThread, "mInitialApplication", application);
            ReflectionHelpers.callInstanceMethodReflectively(application, "attach", new ClassParameter(Context.class, contextImpl));

            appResources.updateConfiguration(configuration, appResources.getDisplayMetrics());
            shadowOf(appResources.getAssets()).setQualifiers(qualifiers);
            shadowOf(application).setStrictI18n(strictI18n);

            RuntimeEnvironment.application = application;
            application.onCreate();
        }
    }

    @Override
    public void tearDownApplication() {
        if (RuntimeEnvironment.application != null) {
            RuntimeEnvironment.application.onTerminate();
        }
    }

    @Override
    public Object getCurrentApplication() {
        return RuntimeEnvironment.application;
    }

    @Override
    public void setSdkConfig(SdkConfig sdkConfig) {
        this.sdkConfig = sdkConfig;
    }

    // Robospock specific code goes here

    public final ResourceLoader getAppResourceLoader(SdkConfig sdkConfig, ResourceLoader systemResourceLoader, final AndroidManifest appManifest) {
        Pair<AndroidManifest, SdkConfig> androidManifestSdkConfigPair = new Pair<AndroidManifest, SdkConfig>(appManifest, sdkConfig);
        ResourceLoader resourceLoader = resourceLoadersByManifestAndConfig.get(androidManifestSdkConfigPair);
        if (resourceLoader == null) {
            resourceLoader = createAppResourceLoader(systemResourceLoader, appManifest);
            resourceLoadersByManifestAndConfig.put(androidManifestSdkConfigPair, resourceLoader);
        }
        return resourceLoader;
    }

    protected ResourceLoader createAppResourceLoader(ResourceLoader systemResourceLoader, AndroidManifest appManifest) {
        List<PackageResourceLoader> appAndLibraryResourceLoaders = new ArrayList<PackageResourceLoader>();
        for (ResourcePath resourcePath : appManifest.getIncludedResourcePaths()) {
            appAndLibraryResourceLoaders.add(createResourceLoader(resourcePath));
        }
        OverlayResourceLoader overlayResourceLoader = new OverlayResourceLoader(appManifest.getPackageName(), appAndLibraryResourceLoaders);

        Map<String, ResourceLoader> resourceLoaders = new HashMap<String, ResourceLoader>();
        resourceLoaders.put("android", systemResourceLoader);
        resourceLoaders.put(appManifest.getPackageName(), overlayResourceLoader);
        return new RoutingResourceLoader(resourceLoaders);
    }

    public PackageResourceLoader createResourceLoader(ResourcePath resourcePath) {
        return new PackageResourceLoader(resourcePath);
    }
}