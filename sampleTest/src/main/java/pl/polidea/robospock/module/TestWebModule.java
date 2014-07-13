package pl.polidea.robospock.module;

import com.google.inject.AbstractModule;
import org.mockito.Mockito;
import pl.polidea.robospock.web.WebInterface;

public class TestWebModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WebInterface.class).toInstance(Mockito.mock(WebInterface.class));
    }

}
