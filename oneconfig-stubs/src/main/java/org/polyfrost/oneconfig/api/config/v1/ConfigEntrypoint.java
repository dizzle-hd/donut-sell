package org.polyfrost.oneconfig.api.config.v1;

/** Compile-only stub, see oneconfig-stubs/build.gradle.kts. */
public interface ConfigEntrypoint {
    String ENTRYPOINT = "oneconfig:config";

    void registerConfigs(Context context);

    interface Context {
        String modId();

        <T extends Config> T register(T config);

        void originalScreen(Runnable opener);
    }
}
