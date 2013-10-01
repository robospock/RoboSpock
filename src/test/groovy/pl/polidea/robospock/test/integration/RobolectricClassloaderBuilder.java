package pl.polidea.robospock.test.integration;

import org.robolectric.AndroidManifest;
import org.robolectric.MavenCentral;
import org.robolectric.SdkConfig;
import org.robolectric.annotation.Config;
import org.robolectric.bytecode.AsmInstrumentingClassLoader;
import org.robolectric.bytecode.Setup;
import org.robolectric.res.Fs;

import java.net.URL;

public class RobolectricClassloaderBuilder {
    private static final MavenCentral MAVEN_CENTRAL = new MavenCentral();

    private AndroidManifest androidManifest;
    private Config implementation;

    public RobolectricClassloaderBuilder(Config implementation) {
        this.implementation = implementation;
    }

    public ClassLoader build() {

        return this.getClass().getClassLoader();
    }
}
