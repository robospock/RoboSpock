package pl.polidea.robospock.module;

import com.google.inject.AbstractModule;
import pl.polidea.robospock.web.WebInterface;
import pl.polidea.robospock.web.WebInterfaceImpl;

public class WebModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WebInterface.class).to(WebInterfaceImpl.class);
    }

}
