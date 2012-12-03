package pl.polidea.tddandroid.module;

import org.mockito.Mockito;

import pl.polidea.tddandroid.web.WebInterface;

import com.google.inject.AbstractModule;

public class TestWebModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(WebInterface.class).toInstance(Mockito.mock(WebInterface.class));
    }

}
