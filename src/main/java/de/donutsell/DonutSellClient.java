package de.donutsell;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class DonutSellClient implements ClientModInitializer {
    public static final String MOD_ID = "donutsell";

    private static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        toggleKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.donutsell.toggle", InputConstants.KEY_K, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (toggleKey.consumeClick()) AutoSeller.INSTANCE.toggle();
            AutoSeller.INSTANCE.tick(mc);
        });

        // server (system) messages, e.g. "you have reached the maximum amount of listings"
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) AutoSeller.INSTANCE.onChatMessage(message.getString());
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (AutoSeller.INSTANCE.isRunning()) AutoSeller.INSTANCE.stop("Verbindung getrennt.");
        });
    }
}
