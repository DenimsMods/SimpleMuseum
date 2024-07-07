package dev.denimred.simplemuseum;

import dev.denimred.simplemuseum.init.SMEntityTypes;
import dev.denimred.simplemuseum.puppet.PuppetRenderer;
import dev.denimred.simplemuseum.puppet.data.SyncPuppetFacets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

import static dev.denimred.simplemuseum.SimpleMuseum.LOGGER;

public final class SimpleMuseumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LOGGER.debug("Simple Museum - Client Init");
        EntityRendererRegistry.register(SMEntityTypes.PUPPET, PuppetRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(SyncPuppetFacets.TYPE, (packet, player, responder) -> packet.handle(player.clientLevel));
    }
}
