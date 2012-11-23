package pl.polidea.robospock;

import android.app.Application;
import com.xtremelabs.robolectric.ApplicationResolver;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricConfig;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.shadows.ShadowApplication;
import com.xtremelabs.robolectric.util.DatabaseConfig;
import com.xtremelabs.robolectric.util.SQLiteMap;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;

import java.io.File;
import java.util.*;

public class RobolectricSpockInterceptor extends AbstractMethodInterceptor {

    final private List<Class<?>> shadowClasses = new LinkedList<Class<?>>();

    private static Map<RobolectricConfig, ResourceLoader> resourceLoaderForRootAndDirectory = new HashMap<RobolectricConfig, ResourceLoader>();

    public static ResourceLoader createResourceLoader(final RobolectricConfig robolectricConfig) {
        ResourceLoader resourceLoader = resourceLoaderForRootAndDirectory.get(robolectricConfig);
        if (resourceLoader == null) {
            try {
                robolectricConfig.validate();

                String rClassName = robolectricConfig.getRClassName();
                Class rClass = Class.forName(rClassName);
                resourceLoader = new ResourceLoader(robolectricConfig.getRealSdkVersion(), rClass, robolectricConfig.getResourceDirectory(), robolectricConfig.getAssetsDirectory());
                resourceLoaderForRootAndDirectory.put(robolectricConfig, resourceLoader);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        resourceLoader.setStrictI18n(robolectricConfig.getStrictI18n());

        resourceLoader.setLayoutQualifierSearchPath();

        return resourceLoader;
    }

    public RobolectricSpockInterceptor(Set<Class<?>> shadowClasses) {
        this.shadowClasses.addAll(shadowClasses);
    }

    @Override
    public void interceptInitializerMethod(IMethodInvocation invocation) throws Throwable {
        resetAndSetup(invocation);
    }

    protected void resetAndSetup(IMethodInvocation invocation) throws Throwable {
        Robolectric.bindDefaultShadowClasses();

        Robolectric.bindShadowClasses(shadowClasses);

        Robolectric.resetStaticState();

        // this creates new (empty) database (held statically! if app is already running it'll not clean it)
        DatabaseConfig.setDatabaseMap(new SQLiteMap());

        // create base Application
        RobolectricConfig config = new RobolectricConfig(new File("."));
        Application application = new ApplicationResolver(config).resolveApplication();

        // proceed with creating resource loader and binding to static Robolectric reference
        ResourceLoader resourceLoader = createResourceLoader(config);
        Robolectric.application = ShadowApplication.bind(application, resourceLoader);

        application.onCreate();

        invocation.proceed();
    }
}
