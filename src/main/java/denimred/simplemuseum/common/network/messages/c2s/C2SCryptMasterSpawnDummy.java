package denimred.simplemuseum.common.network.messages.c2s;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import denimred.simplemuseum.common.entity.MuseumDummyEntity;

public class C2SCryptMasterSpawnDummy {
    private final Vector3d pos;

    public C2SCryptMasterSpawnDummy(Vector3d pos) {
        this.pos = pos;
    }

    public static C2SCryptMasterSpawnDummy decode(PacketBuffer buf) {
        final double x = buf.readDouble();
        final double y = buf.readDouble();
        final double z = buf.readDouble();
        return new C2SCryptMasterSpawnDummy(new Vector3d(x, y, z));
    }

    public void encode(PacketBuffer buf) {
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
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
            if (world.isBlockLoaded(new BlockPos(pos))) {
                // TODO: Do permissions check to avoid hacker griefing
                MuseumDummyEntity.spawn(world, pos, sender.getPositionVec());
            }
        }
    }
}
