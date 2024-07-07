package dev.denimred.simplemuseum.mixin;

import dev.denimred.simplemuseum.puppet.Puppet;
import dev.denimred.simplemuseum.puppet.data.SyncPuppetFacets;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.denimred.simplemuseum.SimpleMuseum.LOGGER;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {
    @Shadow
    @Final
    private Entity entity;

    @Shadow
    protected abstract void broadcastAndSend(Packet<?> packet);

    @Inject(method = "sendDirtyEntityData", at = @At("RETURN"))
    private void syncDirtyPuppetFacets(CallbackInfo ci) {
        if (entity instanceof Puppet puppet) {
            var instances = puppet.facets().getDirtyInstances();
            if (!instances.isEmpty()) {
                LOGGER.trace("Syncing puppet #{} dirty facets to clients", puppet.getId());
                broadcastFabricPacket(new SyncPuppetFacets(puppet, instances));
            }
            instances.clear();
        }
    }

    @Unique
    private void broadcastFabricPacket(FabricPacket packet) {
        var buf = PacketByteBufs.create();
        packet.write(buf);
        broadcastAndSend(ServerPlayNetworking.createS2CPacket(packet.getType().getId(), buf));
    }
}
