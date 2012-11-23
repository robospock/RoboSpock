package pl.polidea.robospock;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import org.spockframework.runtime.extension.ExtensionAnnotation;
import com.google.inject.Module;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtensionAnnotation(RobolectricGuiceExtension.class)
@interface RobolectricGuiceModules {

    Class<? extends Module>[] value() default {};
}
