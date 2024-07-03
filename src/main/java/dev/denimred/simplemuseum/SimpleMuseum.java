package dev.denimred.simplemuseum;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SimpleMuseum implements ModInitializer {
    public static final String MOD_ID = "simplemuseum";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Simple Museum Common Init");
    }
}
