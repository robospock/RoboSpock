package pl.polidea.robospock;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.util.Modules;
import com.xtremelabs.robolectric.Robolectric;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.SpecInfo;
import roboguice.RoboGuice;
import spock.lang.Shared;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RobolectricGuiceInterceptor extends AbstractMethodInterceptor {

    private final Set<Class<? extends Module>> moduleClasses;
    private final Set<InjectionPoint> injectionPoints;

    private Injector getInjector() {
        return RoboGuice.getInjector(Robolectric.application);
    }

    public RobolectricGuiceInterceptor(SpecInfo spec, Set<Class<? extends Module>> moduleClasses) {
        this.moduleClasses = moduleClasses;
        injectionPoints = InjectionPoint.forInstanceMethodsAndFields(spec.getReflection());
    }

    @Override
    public void interceptInitializerMethod(IMethodInvocation invocation) throws Throwable {
        invocation.proceed();

        rebuildRoboGuiceInjector();

        injectValues(invocation.getTarget());
    }

    private void rebuildRoboGuiceInjector() {
        RoboGuice.setBaseApplicationInjector(
                Robolectric.application,
                RoboGuice.DEFAULT_STAGE,
                Modules.override(
                        RoboGuice.newDefaultRoboModule(Robolectric.application)
                ).with(createModules())
        );
    }

    private List<Module> createModules() {
        List<Module> modules = new ArrayList<Module>();
        for (Class<? extends Module> clazz : moduleClasses) {
            try {
                modules.add(clazz.newInstance());
            } catch (InstantiationException e) {
                throw new RuntimeException(
                        String.format("Failed to instantiate module '%s'", clazz.getSimpleName()),
                        e
                );
            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                        String.format("Failed to instantiate module '%s'", clazz.getSimpleName()),
                        e
                );
            }
        }
        return modules;
    }

    private void injectValues(Object target) throws IllegalAccessException {
        for (InjectionPoint point : injectionPoints) {
            if (!(point.getMember() instanceof Field))
                throw new RuntimeException(
                        "Method injection is not supported; use field injection instead"
                );

            Field field = (Field)point.getMember();
            if (field.isAnnotationPresent(Shared.class)) {
                throw new RuntimeException(
                        "Shared field injection is not supported; use field injection instead"
                );
            }

            Object value = getInjector().getInstance(point.getDependencies().get(0).getKey());
            field.setAccessible(true);
            field.set(target, value);
        }
    }

}
