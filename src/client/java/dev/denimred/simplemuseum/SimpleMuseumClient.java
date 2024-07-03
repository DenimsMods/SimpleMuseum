package dev.denimred.simplemuseum;

import net.fabricmc.api.ClientModInitializer;

import static dev.denimred.simplemuseum.SimpleMuseum.LOGGER;

public final class SimpleMuseumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LOGGER.info("Simple Museum Client Init");
    }
}
