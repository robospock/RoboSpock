package pl.polidea.robospock;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;

import android.app.Application;

import com.xtremelabs.robolectric.ApplicationResolver;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricConfig;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.shadows.ShadowApplication;
import com.xtremelabs.robolectric.util.DatabaseConfig;
import com.xtremelabs.robolectric.util.SQLiteMap;

public class RobolectricSpockInterceptor extends AbstractMethodInterceptor {

    final private List<Class< ? >> shadowClasses = new LinkedList<Class< ? >>();

    private static Map<RobolectricConfig, ResourceLoader> resourceLoaderForRootAndDirectory = new HashMap<RobolectricConfig, ResourceLoader>();

    public static ResourceLoader createResourceLoader(final RobolectricConfig robolectricConfig) {
        ResourceLoader resourceLoader = resourceLoaderForRootAndDirectory.get(robolectricConfig);
        if (resourceLoader == null) {
            try {
                robolectricConfig.validate();

                final String rClassName = robolectricConfig.getRClassName();
                final Class rClass = Class.forName(rClassName);
                resourceLoader = new ResourceLoader(robolectricConfig.getRealSdkVersion(), rClass,
                        robolectricConfig.getResourceDirectory(), robolectricConfig.getAssetsDirectory());
                resourceLoaderForRootAndDirectory.put(robolectricConfig, resourceLoader);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        resourceLoader.setStrictI18n(robolectricConfig.getStrictI18n());

        resourceLoader.setLayoutQualifierSearchPath();

        return resourceLoader;
    }

    public RobolectricSpockInterceptor(final Set<Class< ? >> shadowClasses) {
        this.shadowClasses.addAll(shadowClasses);
    }

    @Override
    public void interceptInitializerMethod(final IMethodInvocation invocation) throws Throwable {
        resetAndSetup(invocation);
    }

    protected void resetAndSetup(final IMethodInvocation invocation) throws Throwable {
        Robolectric.bindDefaultShadowClasses();

        Robolectric.bindShadowClasses(shadowClasses);

        Robolectric.resetStaticState();

        // this creates new (empty) database (held statically! if app is already
        // running it'll not clean it)
        DatabaseConfig.setDatabaseMap(new SQLiteMap());

        // create base Application
        final RobolectricConfig config = new RobolectricConfig(new File("."));
        final Application application = new ApplicationResolver(config).resolveApplication();

        // proceed with creating resource loader and binding to static
        // Robolectric reference
        final ResourceLoader resourceLoader = createResourceLoader(config);
        Robolectric.application = ShadowApplication.bind(application, resourceLoader);

        invocation.proceed();
    }
}
