package pl.polidea.robospock.test.integration;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.*;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.bytecode.AsmInstrumentingClassLoader;
import org.robolectric.bytecode.ClassHandler;
import org.robolectric.bytecode.ShadowMap;
import org.robolectric.bytecode.ShadowWrangler;
import org.robolectric.res.Fs;
import org.robolectric.util.AnnotationUtil;
import org.spockframework.runtime.Sputnik;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

public class RoboSputnik extends Runner implements Filterable, Sortable {

    private static final MavenCentral MAVEN_CENTRAL = new MavenCentral();

    private Object sputnik;

    public RoboSputnik(Class<?> clazz) throws InitializationError {
//        ClassLoader classLoader = new RobolectricClassloaderBuilder(
//               AnnotationUtil.defaultsFor(Config.class)
//        ).build();

        AndroidManifest androidManifest = new AndroidManifest(Fs.currentDirectory().join("AndroidManifest.xml"),
                Fs.currentDirectory().join("res"),
                Fs.currentDirectory().join("assets"));

        URL[] urls = MAVEN_CENTRAL.getLocalArtifactUrls(
                null,
                new SdkConfig("4.1.2_r1_rc").getSdkClasspathDependencies()
        ).values().toArray(new URL[0]);

        ClassLoader classLoader = new AsmInstrumentingClassLoader(new RoboSpockSetup(), urls);

        SdkEnvironment sdkEnvironment = new SdkEnvironment(new SdkConfig("4.1.2_r1_rc"), classLoader);


        ShadowMap shadowMap = new ShadowMap.Builder().build();

        ClassHandler classHandler = new ShadowWrangler(shadowMap);
        RobolectricTestRunner.injectClassHandler(sdkEnvironment.getRobolectricClassLoader(), classHandler);

        try {

            this.sputnik = sdkEnvironment
                    .bootstrappedClass(Sputnik.class)
                    .getConstructor(Class.class)
                    .newInstance(sdkEnvironment.bootstrappedClass(clazz));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Description getDescription() {
        return ((Runner) sputnik).getDescription();
    }

    public void run(RunNotifier notifier) {
        ((Runner) sputnik).run(notifier);
    }

    public void filter(Filter filter) throws NoTestsRemainException {
        ((Filterable) sputnik).filter(filter);
    }

    public void sort(Sorter sorter) {
        ((Sortable) sputnik).sort(sorter);
    }


}
