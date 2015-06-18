package com.example.robospock.module;

import com.example.robospock.web.WebInterface;
import com.google.inject.AbstractModule;
import org.mockito.Mockito;

public class TestWebModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WebInterface.class).toInstance(Mockito.mock(WebInterface.class));
    }

}
