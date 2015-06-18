package com.example.robospock;

import com.example.robospock.module.TaskExecutorModule;
import com.example.robospock.module.WebModule;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.util.Modules;

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
