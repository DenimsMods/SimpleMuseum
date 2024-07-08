package dev.denimred.simplemuseum.init;

import dev.denimred.simplemuseum.puppet.entity.Puppet;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;

import static dev.denimred.simplemuseum.SimpleMuseum.LOGGER;
import static dev.denimred.simplemuseum.SimpleMuseum.id;
import static net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder.createLiving;

public final class SMEntityTypes {
    public static final EntityType<Puppet> PUPPET = entity("puppet", createLiving().defaultAttributes(Puppet::createAttributes).dimensions(EntityDimensions.scalable(0.6F, 1.8F)).trackRangeChunks(10).entityFactory(Puppet::new));

    @ApiStatus.Internal
    public static void register() {
        LOGGER.debug("Registered Entity Types");
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends Entity> EntityType<T> entity(String id, FabricEntityTypeBuilder<T> builder) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, id(id), builder.build());
    }
}
