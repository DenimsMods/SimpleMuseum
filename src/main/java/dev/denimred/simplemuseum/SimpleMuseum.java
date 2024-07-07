package dev.denimred.simplemuseum;

import dev.denimred.simplemuseum.init.SMEntityTypes;
import dev.denimred.simplemuseum.init.SMItems;
import dev.denimred.simplemuseum.init.SMPuppetFacets;
import dev.denimred.simplemuseum.puppet.PuppetCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SimpleMuseum implements ModInitializer {
    public static final String MOD_ID = "simplemuseum";
    public static final Logger LOGGER = LoggerFactory.getLogger("SimpleMuseum");

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.debug("Simple Museum - Common Init");
        SMEntityTypes.register();
        SMItems.register();
        SMPuppetFacets.register();
        CommandRegistrationCallback.EVENT.register(PuppetCommands::register);
    }
}
