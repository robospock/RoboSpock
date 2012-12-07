package pl.polidea.tddandroid;

import android.app.Application;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.runners.model.InitializationError;
import pl.polidea.tddandroid.module.TestTaskExecutorModule;
import pl.polidea.tddandroid.module.TestWebModule;
import pl.polidea.tddandroid.shadow.MyActivityManagerShadow;
import roboguice.RoboGuice;

public class MyTestRunner extends RobolectricTestRunner {
    public MyTestRunner(final Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    public void prepareTest(final Object test) {
        final TddApplication application = (TddApplication) Robolectric.application;
        RoboGuice.injectMembers(application, test);
    }

    @Override
    protected Application createApplication() {
        final TddApplication application = (TddApplication) super.createApplication();
        application.onCreate();
        application.setModules(new TestTaskExecutorModule(), new TestWebModule());
        return application;
    }

    @Override
    protected void bindShadowClasses() {
        Robolectric.bindShadowClass(MyActivityManagerShadow.class);
    }

}
