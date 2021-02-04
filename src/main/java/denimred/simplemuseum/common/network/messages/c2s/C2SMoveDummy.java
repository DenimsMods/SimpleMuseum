package denimred.simplemuseum.common.network.messages.c2s;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

import denimred.simplemuseum.common.entity.MuseumDummyEntity;

public class C2SMoveDummy {
    private final UUID uuid;
    private final Vector3d pos;
    private final float yaw;

    public C2SMoveDummy(UUID uuid, Vector3d pos, float yaw) {
        this.uuid = uuid;
        this.pos = pos;
        this.yaw = yaw;
    }

    public static C2SMoveDummy decode(PacketBuffer buf) {
        final UUID dummy = buf.readUniqueId();
        final double x = buf.readDouble();
        final double y = buf.readDouble();
        final double z = buf.readDouble();
        final float yaw = buf.readFloat();
        return new C2SMoveDummy(dummy, new Vector3d(x, y, z), yaw);
    }

    public void encode(PacketBuffer buf) {
        buf.writeUniqueId(uuid);
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeFloat(yaw);
    }

    public void handle(Supplier<NetworkEvent.Context> sup) {
        final NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> doWork(ctx));
        ctx.setPacketHandled(true);
    }

    @SuppressWarnings("deprecation") // Mojang >:I
    private void doWork(NetworkEvent.Context ctx) {
        final ServerPlayerEntity sender = ctx.getSender();
        if (sender != null) {
            final ServerWorld world = sender.getServerWorld();
            final Entity entity = world.getEntityByUuid(uuid);
            if (entity instanceof MuseumDummyEntity) {
                final MuseumDummyEntity dummy = (MuseumDummyEntity) entity;
                if (dummy.isAlive()
                        && world.isBlockLoaded(new BlockPos(dummy.getPositionVec()))
                        && world.isBlockLoaded(new BlockPos(pos))) {
                    // TODO: Do permissions check to avoid hacker griefing
                    dummy.setLocationAndAngles(
                            pos.x, pos.y, pos.z, MathHelper.wrapDegrees(yaw), dummy.rotationPitch);
                }
            }
        }
    }
}
