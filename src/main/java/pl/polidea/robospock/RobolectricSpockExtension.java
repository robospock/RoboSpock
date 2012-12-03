package pl.polidea.robospock;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.SpecInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RobolectricSpockExtension extends AbstractAnnotationDrivenExtension<UseShadows> {

    private final Set<Class<?>> shadowClasses = new HashSet<Class<?>>();

    @Override
    public void visitSpecAnnotation(UseShadows annotation, SpecInfo spec) {
        shadowClasses.addAll(Arrays.asList(annotation.value()));
    }

    @Override
    public void visitSpec(SpecInfo spec) {
        RobolectricSpockInterceptor interceptor = new RobolectricSpockInterceptor(shadowClasses);

        spec.getTopSpec().getInitializerMethod().addInterceptor(interceptor);
    }
}
