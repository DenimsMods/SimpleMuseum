package denimred.simplemuseum.common.network;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Optional;

import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;

public final class NetworkUtil {
    @SuppressWarnings("deprecation") // Mojang >:I
    public static Optional<PuppetEntity> getValidPuppet(NetworkEvent.Context ctx, int puppetId) {
        if (ctx.getDirection().getReceptionSide().isServer()) {
            final ServerPlayerEntity sender = ctx.getSender();
            if (sender != null) {
                final ServerWorld world = sender.getServerWorld();
                final Entity entity = world.getEntityByID(puppetId);
                if (entity instanceof PuppetEntity
                        && world.isBlockLoaded(entity.getPosition())
                        && ((PuppetEntity) entity).exists()) {
                    return Optional.of((PuppetEntity) entity);
                }
            }
        } else {
            final ClientWorld world = ClientUtil.MC.world;
            if (world != null) {
                final Entity entity = world.getEntityByID(puppetId);
                if (entity instanceof PuppetEntity) {
                    return Optional.of((PuppetEntity) entity);
                }
            }
        }
        return Optional.empty();
    }
}
