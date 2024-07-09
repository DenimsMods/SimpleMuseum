package dev.denimred.simplemuseum;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.denimred.simplemuseum.init.SMEntityTypes;
import dev.denimred.simplemuseum.puppet.PuppetRenderer;
import dev.denimred.simplemuseum.puppet.data.PuppetFacetGroup;
import dev.denimred.simplemuseum.puppet.data.SyncPuppetEntityFacets;
import dev.denimred.simplemuseum.puppet.edit.OpenPuppetFacetsEditScreen;
import dev.denimred.simplemuseum.puppet.gui.PuppetFacetsEditScreen;
import dev.denimred.simplemuseum.puppet.gui.subscreen.DefaultSubscreen;
import dev.denimred.simplemuseum.puppet.gui.subscreen.SubscreenFactory;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

import java.util.HashMap;
import java.util.Map;

import static dev.denimred.simplemuseum.SimpleMuseum.LOGGER;

public final class SimpleMuseumClient implements ClientModInitializer {
    private static final Map<PuppetFacetGroup, SubscreenFactory> DEFAULT_SUBSCREENS = new HashMap<>();
    private static final Table<PuppetFacetGroup, String, SubscreenFactory> SECTION_SUBSCREENS = HashBasedTable.create();

    public static SubscreenFactory getSubscreenFactory(PuppetFacetGroup group, String section) {
        var sectionSubscreen = SECTION_SUBSCREENS.get(group, section);
        if (sectionSubscreen != null) return sectionSubscreen;
        return DEFAULT_SUBSCREENS.getOrDefault(group, DefaultSubscreen::new);
    }

    public static void registerDefaultSubscreen(PuppetFacetGroup group, SubscreenFactory subscreen) {
        DEFAULT_SUBSCREENS.put(group, subscreen);
    }

    public static void registerSectionSubscreen(PuppetFacetGroup group, String section, SubscreenFactory subscreen) {
        SECTION_SUBSCREENS.put(group, section, subscreen);
    }

    @Override
    public void onInitializeClient() {
        LOGGER.debug("Simple Museum - Client Init");
        EntityRendererRegistry.register(SMEntityTypes.PUPPET, PuppetRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(SyncPuppetEntityFacets.TYPE, (packet, player, responder) -> packet.handle(player.clientLevel));
        ClientPlayNetworking.registerGlobalReceiver(OpenPuppetFacetsEditScreen.TYPE, (packet, player, responder) -> PuppetFacetsEditScreen.open(packet, player));
    }
}
