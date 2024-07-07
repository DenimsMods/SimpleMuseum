package dev.denimred.simplemuseum.init;

import dev.denimred.simplemuseum.cane.CuratorsCaneItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;

import static dev.denimred.simplemuseum.SimpleMuseum.LOGGER;
import static dev.denimred.simplemuseum.SimpleMuseum.id;

public final class SMItems {
    public static final CuratorsCaneItem CURATORS_CANE = item("curators_cane", new CuratorsCaneItem());

    @ApiStatus.Internal
    public static void register() {
        LOGGER.debug("Registered Items");
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends Item> T item(String id, T item) {
        return Registry.register(BuiltInRegistries.ITEM, id(id), item);
    }
}
