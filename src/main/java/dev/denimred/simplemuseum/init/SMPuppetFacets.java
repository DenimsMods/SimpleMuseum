package dev.denimred.simplemuseum.init;

import dev.denimred.simplemuseum.puppet.data.PuppetFacet;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import static dev.denimred.simplemuseum.SimpleMuseum.LOGGER;
import static dev.denimred.simplemuseum.SimpleMuseum.id;

public final class SMPuppetFacets {
    public static final ResourceKey<Registry<PuppetFacet<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(id("puppet_facet"));
    public static final Registry<PuppetFacet<?>> REGISTRY = FabricRegistryBuilder.createSimple(REGISTRY_KEY).attribute(RegistryAttribute.SYNCED).buildAndRegister();

    public static final PuppetFacet<ResourceLocation> MODEL = facet("model", new PuppetFacet<>(id("geo/entity/puppet.geo.json"), ResourceLocation.CODEC));
    public static final PuppetFacet<ResourceLocation> TEXTURE = facet("texture", new PuppetFacet<>(id("textures/entity/puppet.png"), ResourceLocation.CODEC));
    public static final PuppetFacet<ResourceLocation> ANIMATIONS = facet("animations", new PuppetFacet<>(id("animations/entity/puppet.animation.json"), ResourceLocation.CODEC));

    @ApiStatus.Internal
    public static void register() {
        LOGGER.debug("Registered Puppet Facets");
    }

    private static <T, V extends PuppetFacet<T>> V facet(String id, V value) {
        return Registry.register(REGISTRY, id(id), value);
    }
}
