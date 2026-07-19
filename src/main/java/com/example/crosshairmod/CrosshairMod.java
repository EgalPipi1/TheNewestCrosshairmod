package com.example.crosshairmod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrosshairMod implements ModInitializer {

    public static final String MOD_ID = "crosshairmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Multi Crosshair mod loaded");
    }
}
