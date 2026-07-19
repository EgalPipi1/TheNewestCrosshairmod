package com.example.crosshairmod.client;

import com.example.crosshairmod.CrosshairMod;
import com.example.crosshairmod.command.CrosshairCommand;
import com.example.crosshairmod.config.CrosshairConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class CrosshairModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CrosshairConfig.load();
        CrosshairCommand.register();
        HudRenderCallback.EVENT.register(CrosshairHudRenderer::render);

        CrosshairInteractionHandler interactionHandler = new CrosshairInteractionHandler();
        ClientTickEvents.END_CLIENT_TICK.register(interactionHandler::onEndTick);

        CrosshairMod.LOGGER.info("Multi Crosshair client features initialized ({} crosshairs configured)",
                CrosshairConfig.count);
    }
}
