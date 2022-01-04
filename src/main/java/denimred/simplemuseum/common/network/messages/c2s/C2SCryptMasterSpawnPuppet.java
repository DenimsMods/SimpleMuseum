package denimred.simplemuseum.common.network.messages.c2s;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;

public class C2SCryptMasterSpawnPuppet {
    private final Vec3 pos;

    public C2SCryptMasterSpawnPuppet(Vec3 pos) {
        this.pos = pos;
    }

    public static C2SCryptMasterSpawnPuppet decode(FriendlyByteBuf buf) {
        final double x = buf.readDouble();
        final double y = buf.readDouble();
        final double z = buf.readDouble();
        return new C2SCryptMasterSpawnPuppet(new Vec3(x, y, z));
    }

    public void encode(FriendlyByteBuf buf) {
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
        final ServerPlayer sender = ctx.getSender();
        if (sender != null) {
            final ServerLevel world = sender.getLevel();
            if (world.hasChunkAt(new BlockPos(pos))) {
                // TODO: Do permissions check?
                PuppetEntity.spawn(world, pos, sender.position());
            }
        }
    }
}
