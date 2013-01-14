package pl.polidea.tddandroid.module;

import com.google.inject.AbstractModule;
import pl.polidea.tddandroid.web.WebInterface;
import pl.polidea.tddandroid.web.WebInterfaceImpl;

public class WebModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WebInterface.class).to(WebInterfaceImpl.class);
    }

}
