package pl.polidea.robospock;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.SpecInfo;
import com.google.inject.Module;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RobolectricGuiceExtension extends AbstractAnnotationDrivenExtension<RobolectricGuiceModules> {
    private final Set<Class<? extends Module>> moduleClasses = new HashSet<Class<? extends Module>>();

    @Override
    public void visitSpecAnnotation(RobolectricGuiceModules robolectricGuiceModules, SpecInfo spec) {
        moduleClasses.addAll(Arrays.asList(robolectricGuiceModules.value()));
    }

    @Override
    public void visitSpec(SpecInfo spec) {
        if (moduleClasses.isEmpty()) return;

        RobolectricGuiceInterceptor interceptor = new RobolectricGuiceInterceptor(spec, moduleClasses);
        SpecInfo topSpec = spec.getTopSpec();
        topSpec.getInitializerMethod().addInterceptor(interceptor);
    }
}
