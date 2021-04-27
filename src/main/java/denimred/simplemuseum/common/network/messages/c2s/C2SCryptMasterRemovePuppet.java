package denimred.simplemuseum.common.network.messages.c2s;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

import denimred.simplemuseum.common.entity.MuseumPuppetEntity;

public class C2SCryptMasterRemovePuppet {
    private final UUID uuid;

    public C2SCryptMasterRemovePuppet(UUID uuid) {
        this.uuid = uuid;
    }

    public static C2SCryptMasterRemovePuppet decode(PacketBuffer buf) {
        final UUID uuid = buf.readUniqueId();
        return new C2SCryptMasterRemovePuppet(uuid);
    }

    public void encode(PacketBuffer buf) {
        buf.writeUniqueId(uuid);
    }

    public void handle(Supplier<NetworkEvent.Context> sup) {
        final NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> doWork(ctx));
        ctx.setPacketHandled(true);
    }

    @SuppressWarnings("deprecation") // >:I Mojang
    private void doWork(NetworkEvent.Context ctx) {
        final ServerPlayerEntity sender = ctx.getSender();
        if (sender != null) {
            final ServerWorld world = sender.getServerWorld();
            final Entity entity = world.getEntityByUuid(uuid);
            if (entity instanceof MuseumPuppetEntity) {
                if (world.isBlockLoaded(entity.getPosition())
                        && ((MuseumPuppetEntity) entity).exists()) {
                    // TODO: Do permissions check?
                    entity.remove();
                }
            }
        }
    }
}
