package dev.denimred.simplemuseum.datagen;

import dev.denimred.simplemuseum.SimpleMuseum;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

import static dev.denimred.simplemuseum.SimpleMuseum.LOGGER;

public final class SimpleMuseumDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator gen) {
        LOGGER.debug("Simple Museum - Datagen Init");
        var pack = gen.createPack();
        pack.addProvider(SMLanguageProvider::new);
        pack.addProvider(SMModelProvider::new);
    }

    @Override
    public String getEffectiveModId() {
        return SimpleMuseum.MOD_ID;
    }
}
