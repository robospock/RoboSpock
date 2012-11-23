package pl.polidea.tddandroid.module;

import pl.polidea.tddandroid.web.WebInterface;
import pl.polidea.tddandroid.web.WebInterfaceImpl;

import com.google.inject.AbstractModule;

public class WebModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(WebInterface.class).to(WebInterfaceImpl.class);
    }

}
