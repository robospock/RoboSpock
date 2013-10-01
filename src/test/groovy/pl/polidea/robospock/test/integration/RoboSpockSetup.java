package pl.polidea.robospock.test.integration;

import org.robolectric.bytecode.Setup;
import org.spockframework.runtime.Sputnik;

public class RoboSpockSetup extends Setup {

    @Override
    public boolean shouldAcquire(String name) {
        if(name != null && name.startsWith("org.junit")) {
            return false;
        }

        return super.shouldAcquire(name);
    }
}
