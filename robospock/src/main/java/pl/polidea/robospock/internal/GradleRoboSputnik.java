package pl.polidea.robospock.internal;

import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

public abstract class GradleRoboSputnik extends RoboSputnik {

    private static final String BUILD_OUTPUT = "build/intermediates";

    public GradleRoboSputnik(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        Class<?> buildConfigClass = getBuildConfig();
        if (buildConfigClass == Void.class) {
            Logger.error("Please provide valid buildConfig class");
            throw new RuntimeException("Please provide valid buildConfig class");
        }

        final String type = getType(buildConfigClass);
        final String flavor = getFlavor(buildConfigClass);
        final String packageName = getPackageName(buildConfigClass, config.packageName());

        final FileFsFile res;
        final FileFsFile assets;
        final FileFsFile manifest;


        if (FileFsFile.from(BUILD_OUTPUT, "res", "merged").exists()) {
            res = FileFsFile.from(BUILD_OUTPUT, "res", "merged", flavor, type);
        } else if (FileFsFile.from(BUILD_OUTPUT, "res").exists()) {
            res = FileFsFile.from(BUILD_OUTPUT, "res", flavor, type);
        } else {
            res = FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "res");
        }

        if (FileFsFile.from(BUILD_OUTPUT, "assets").exists()) {
            assets = FileFsFile.from(BUILD_OUTPUT, "assets", flavor, type);
        } else {
            assets = FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "assets");
        }

        if (FileFsFile.from(BUILD_OUTPUT, "manifests").exists()) {
            manifest = FileFsFile.from(BUILD_OUTPUT, "manifests", "full", flavor, type, "AndroidManifest.xml");
        } else {
            manifest = FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "AndroidManifest.xml");
        }

        Logger.debug("Robolectric assets directory: " + assets.getPath());
        Logger.debug("   Robolectric res directory: " + res.getPath());
        Logger.debug("   Robolectric manifest path: " + manifest.getPath());
        Logger.debug("    Robolectric package name: " + packageName);
        return new AndroidManifest(manifest, res, assets, packageName);
    }

    private static String getType(Class<?> clazz) {
        try {
            return ReflectionHelpers.getStaticField(clazz, "BUILD_TYPE");
        } catch (Throwable e) {
            return null;
        }
    }

    private static String getFlavor(Class<?> constants) {
        try {
            return ReflectionHelpers.getStaticField(constants, "FLAVOR");
        } catch (Throwable e) {
            return null;
        }
    }

    private static String getPackageName(Class<?> clazz, String packageNameFromConfig) {
        try {
            final String packageName = packageNameFromConfig;
            if (packageName != null && !packageName.isEmpty()) {
                return packageName;
            } else {
                return ReflectionHelpers.getStaticField(clazz, "APPLICATION_ID");
            }
        } catch (Throwable e) {
            return null;
        }
    }

    public abstract Class<?> getBuildConfig();
}