package denimred.simplemuseum.common.network.messages.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.UUID;
import java.util.function.Supplier;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import net.minecraftforge.network.NetworkEvent;

public class C2SCryptMasterRemovePuppet {
    private final UUID uuid;

    public C2SCryptMasterRemovePuppet(UUID uuid) {
        this.uuid = uuid;
    }

    public static C2SCryptMasterRemovePuppet decode(FriendlyByteBuf buf) {
        final UUID uuid = buf.readUUID();
        return new C2SCryptMasterRemovePuppet(uuid);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
    }

    public void handle(Supplier<NetworkEvent.Context> sup) {
        final NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> doWork(ctx));
        ctx.setPacketHandled(true);
    }

    @SuppressWarnings("deprecation") // >:I Mojang
    private void doWork(NetworkEvent.Context ctx) {
        final ServerPlayer sender = ctx.getSender();
        if (sender != null) {
            final ServerLevel world = sender.getLevel();
            final Entity entity = world.getEntity(uuid);
            if (entity instanceof PuppetEntity) {
                if (world.hasChunkAt(entity.blockPosition()) && ((PuppetEntity) entity).exists()) {
                    // TODO: Do permissions check?
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }
}
