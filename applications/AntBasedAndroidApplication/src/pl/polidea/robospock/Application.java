package pl.polidea.robospock;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import pl.polidea.robospock.module.TaskExecutorModule;
import pl.polidea.robospock.module.WebModule;
import roboguice.RoboGuice;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        setModules(new TaskExecutorModule(), new WebModule());
    }

    public void setModules(final AbstractModule... newModule) {
        final Module modules = Modules.override(RoboGuice.newDefaultRoboModule(this)).with(newModule);
        RoboGuice.setBaseApplicationInjector(this, RoboGuice.DEFAULT_STAGE, modules);
    }
}
