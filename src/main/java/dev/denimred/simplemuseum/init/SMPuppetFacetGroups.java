package dev.denimred.simplemuseum.init;

import dev.denimred.simplemuseum.puppet.data.PuppetFacetGroup;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.ApiStatus;

import static dev.denimred.simplemuseum.SimpleMuseum.LOGGER;
import static dev.denimred.simplemuseum.SimpleMuseum.id;

public final class SMPuppetFacetGroups {
    public static final ResourceKey<Registry<PuppetFacetGroup>> REGISTRY_KEY = ResourceKey.createRegistryKey(id("puppet_facet_group"));
    public static final DefaultedRegistry<PuppetFacetGroup> REGISTRY = FabricRegistryBuilder.createDefaulted(REGISTRY_KEY, id("blank")).buildAndRegister();

    public static final PuppetFacetGroup BLANK = PuppetFacetGroup.builder(Integer.MIN_VALUE).register(REGISTRY.getDefaultKey());

    //@formatter:off
    public static final PuppetFacetGroup RENDERING = PuppetFacetGroup.builder(100)
            .section("general")
                .facet(SMPuppetFacets.MODEL)
                .facet(SMPuppetFacets.TEXTURE)
                .facet(SMPuppetFacets.ANIMATIONS)
            .icon(Items.ENDER_EYE)
            .register("rendering");
    //@formatter:on

    @ApiStatus.Internal
    public static void register() {
        LOGGER.debug("Registered Puppet Facet Groups");
    }
}
