package util

import org.junit.runners.model.InitializationError
import pl.polidea.robospock.internal.GradleRoboSputnik

class SampleSputnik extends GradleRoboSputnik {
    public SampleSputnik(Class<?> clazz) throws InitializationError {
        super(clazz);
    }
}