package denimred.simplemuseum.common.network.messages.c2s;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.function.Supplier;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import net.minecraftforge.network.NetworkEvent;

@Deprecated
public class C2SMovePuppet {
    private final UUID uuid;
    private final Vec3 pos;
    private final float pitch;
    private final float yaw;

    public C2SMovePuppet(UUID uuid, Vec3 pos, float pitch, float yaw) {
        this.uuid = uuid;
        this.pos = pos;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public static C2SMovePuppet decode(FriendlyByteBuf buf) {
        final UUID puppet = buf.readUUID();
        final double x = buf.readDouble();
        final double y = buf.readDouble();
        final double z = buf.readDouble();
        final float pitch = buf.readFloat();
        final float yaw = buf.readFloat();
        return new C2SMovePuppet(puppet, new Vec3(x, y, z), pitch, yaw);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeFloat(pitch);
        buf.writeFloat(yaw);
    }

    public void handle(Supplier<NetworkEvent.Context> sup) {
        final NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> doWork(ctx));
        ctx.setPacketHandled(true);
    }

    @SuppressWarnings("deprecation") // Mojang >:I
    private void doWork(NetworkEvent.Context ctx) {
        final ServerPlayer sender = ctx.getSender();
        if (sender != null) {
            final ServerLevel world = sender.getLevel();
            final Entity entity = world.getEntity(uuid);
            if (entity instanceof PuppetEntity) {
                final PuppetEntity puppet = (PuppetEntity) entity;
                if (puppet.exists()
                        && world.hasChunkAt(new BlockPos(puppet.position()))
                        && world.hasChunkAt(new BlockPos(pos))) {
                    // TODO: Do permissions check?
                    puppet.moveTo(pos.x, pos.y, pos.z, Mth.wrapDegrees(yaw), pitch);
                    puppet.yRotO = puppet.yRot;
                    puppet.xRotO = puppet.xRot;
                    puppet.setYHeadRot(puppet.yRot);
                    puppet.setYBodyRot(puppet.yRot);
                }
            }
        }
    }
}
