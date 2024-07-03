package dev.denimred.simplemuseum;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

import static dev.denimred.simplemuseum.SimpleMuseum.LOGGER;

public final class SimpleMuseumDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator gen) {
        LOGGER.info("Simple Museum Datagen Init");
    }
}
