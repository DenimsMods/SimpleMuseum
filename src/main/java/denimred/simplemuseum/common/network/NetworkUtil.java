package denimred.simplemuseum.common.network;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import net.minecraftforge.network.NetworkEvent;

public final class NetworkUtil {
    @SuppressWarnings("deprecation") // Mojang >:I
    public static Optional<PuppetEntity> getValidPuppet(NetworkEvent.Context ctx, int puppetId) {
        if (ctx.getDirection().getReceptionSide().isServer()) {
            final ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                final ServerLevel world = sender.getLevel();
                final Entity entity = world.getEntity(puppetId);
                if (entity instanceof PuppetEntity
                        && world.hasChunkAt(entity.blockPosition())
                        && ((PuppetEntity) entity).exists()) {
                    return Optional.of((PuppetEntity) entity);
                }
            }
        } else {
            final ClientLevel world = ClientUtil.MC.level;
            if (world != null) {
                final Entity entity = world.getEntity(puppetId);
                if (entity instanceof PuppetEntity) {
                    return Optional.of((PuppetEntity) entity);
                }
            }
        }
        return Optional.empty();
    }
}
