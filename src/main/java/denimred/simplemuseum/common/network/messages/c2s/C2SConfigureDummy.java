package denimred.simplemuseum.common.network.messages.c2s;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

import denimred.simplemuseum.common.entity.MuseumDummyEntity;

public class C2SConfigureDummy {
    private final UUID uuid;
    private final int rotation;
    private final ResourceLocation modelLoc;
    private final ResourceLocation texLoc;
    private final ResourceLocation animLoc;

    public C2SConfigureDummy(
            UUID uuid,
            int rotation,
            ResourceLocation modelLoc,
            ResourceLocation texLoc,
            ResourceLocation animLoc) {
        this.uuid = uuid;
        this.rotation = rotation;
        this.modelLoc = modelLoc;
        this.texLoc = texLoc;
        this.animLoc = animLoc;
    }

    public static C2SConfigureDummy decode(PacketBuffer buf) {
        final UUID dummy = buf.readUniqueId();
        final int rotation = buf.readInt();
        final ResourceLocation modelLoc = buf.readResourceLocation();
        final ResourceLocation texLoc = buf.readResourceLocation();
        final ResourceLocation animLoc = buf.readResourceLocation();
        return new C2SConfigureDummy(dummy, rotation, modelLoc, texLoc, animLoc);
    }

    public void encode(PacketBuffer buf) {
        buf.writeUniqueId(uuid);
        buf.writeInt(rotation);
        buf.writeResourceLocation(modelLoc);
        buf.writeResourceLocation(texLoc);
        buf.writeResourceLocation(animLoc);
    }

    public void handle(Supplier<NetworkEvent.Context> sup) {
        final NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> doWork(ctx));
        ctx.setPacketHandled(true);
    }

    @SuppressWarnings("deprecation") // Mojang >:(
    private void doWork(NetworkEvent.Context ctx) {
        final ServerPlayerEntity sender = ctx.getSender();
        if (sender != null) {
            final ServerWorld world = sender.getServerWorld();
            final Entity entity = world.getEntityByUuid(uuid);
            if (entity instanceof MuseumDummyEntity) {
                final MuseumDummyEntity dummy = (MuseumDummyEntity) entity;
                if (world.isBlockLoaded(dummy.getPosition())
                        && sender.getDistanceSq(dummy) < 36
                        && dummy.isAlive()) {
                    dummy.rotationYaw = dummy.prevRotationYaw = rotation % 360;
                    dummy.setModelLocation(modelLoc);
                    dummy.setTextureLocation(texLoc);
                    dummy.setAnimationsLocation(animLoc);
                }
            }
        }
    }
}
