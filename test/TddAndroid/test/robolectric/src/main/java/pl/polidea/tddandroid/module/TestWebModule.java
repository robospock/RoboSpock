package pl.polidea.tddandroid.module;

import com.google.inject.AbstractModule;
import org.mockito.Mockito;
import pl.polidea.tddandroid.web.WebInterface;

public class TestWebModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WebInterface.class).toInstance(Mockito.mock(WebInterface.class));
    }

}
