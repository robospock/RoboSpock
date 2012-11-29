package pl.polidea.robospock;


import com.google.inject.AbstractModule
import com.google.inject.Module
import com.google.inject.util.Modules
import com.xtremelabs.robolectric.Robolectric
import org.junit.runner.RunWith
import roboguice.RoboGuice
import spock.lang.Specification

@RunWith(RoboSputnik)
@UseShadows
public abstract class RoboSpecification extends Specification {

    Set<Class<? extends Module>> moduleClasses = [];


    void modules(Closure closure) {


        modules(new AbstractModule() {
            def bind(Class iface, Class clazz) {
                if (clazz.interfaces.contains(iface)) {
                    bind(iface).toInstance(clazz)
                } else {
                    addError("Instance class " + clazz.getName() + " can't be mapped to " + iface.getName())
                }
            }

            def install(Class c) {
                def module = c.newInstance()
                if (module instanceof AbstractModule) {
                    install(module)
                } else {
                    addError("Installed class: " + c.getName() + " it's not Guice Abstract Module")
                }
            }

            @Override
            protected void configure() {
                closure.delegate = this
                closure.call()
            }
        })
    }

    void modules(Module... modules) {

        moduleClasses.addAll(modules)

        RoboGuice.setBaseApplicationInjector(
                Robolectric.application,
                RoboGuice.DEFAULT_STAGE,
                Modules.override(
                        RoboGuice.newDefaultRoboModule(Robolectric.application)
                ).with(moduleClasses)
        );

        RoboGuice.injectMembers(Robolectric.application, this)
    }

}
