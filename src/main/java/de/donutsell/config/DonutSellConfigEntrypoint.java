package de.donutsell.config;

import org.polyfrost.oneconfig.api.config.v1.ConfigEntrypoint;

/**
 * Registered under the {@code oneconfig:config} entrypoint in fabric.mod.json.
 * OneConfig calls this once after all mods are initialised; creating the config here
 * makes it show up as its own card in the OneConfig / OneClient menu (Right Shift).
 */
public final class DonutSellConfigEntrypoint implements ConfigEntrypoint {
    public static DonutSellConfig config;

    @Override
    public void registerConfigs(Context context) {
        config = new DonutSellConfig();
    }
}
