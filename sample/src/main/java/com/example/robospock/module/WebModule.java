package com.example.robospock.module;

import com.example.robospock.web.WebInterface;
import com.example.robospock.web.WebInterfaceImpl;
import com.google.inject.AbstractModule;

public class WebModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WebInterface.class).to(WebInterfaceImpl.class);
    }

}
