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
    private final ResourceLocation modelLoc;
    private final ResourceLocation texLoc;
    private final ResourceLocation animLoc;
    private final String selAnim;

    public C2SConfigureDummy(
            UUID uuid,
            ResourceLocation modelLoc,
            ResourceLocation texLoc,
            ResourceLocation animLoc,
            String selAnim) {
        this.uuid = uuid;
        this.modelLoc = modelLoc;
        this.texLoc = texLoc;
        this.animLoc = animLoc;
        this.selAnim = selAnim;
    }

    public static C2SConfigureDummy decode(PacketBuffer buf) {
        final UUID dummy = buf.readUniqueId();
        final ResourceLocation modelLoc = buf.readResourceLocation();
        final ResourceLocation texLoc = buf.readResourceLocation();
        final ResourceLocation animLoc = buf.readResourceLocation();
        final String selAnim = buf.readString(32767);
        return new C2SConfigureDummy(dummy, modelLoc, texLoc, animLoc, selAnim);
    }

    public void encode(PacketBuffer buf) {
        buf.writeUniqueId(uuid);
        buf.writeResourceLocation(modelLoc);
        buf.writeResourceLocation(texLoc);
        buf.writeResourceLocation(animLoc);
        buf.writeString(selAnim);
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
                if (world.isBlockLoaded(dummy.getPosition()) && dummy.isAlive()) {
                    // TODO: Do permissions check to avoid hacker griefing
                    dummy.setModelLocation(modelLoc);
                    dummy.setTextureLocation(texLoc);
                    dummy.setAnimationsLocation(animLoc);
                    dummy.setSelectedAnimation(selAnim);
                }
            }
        }
    }
}
