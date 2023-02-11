package denimred.simplemuseum.common.network.messages.s2c;

import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;
import java.util.function.Supplier;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.network.NetworkUtil;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ResurrectPuppetSync {
    private final int puppetId;

    public ResurrectPuppetSync(int puppetId) {
        this.puppetId = puppetId;
    }

    public static void register(SimpleChannel channel, int id) {
        channel.registerMessage(
                id,
                ResurrectPuppetSync.class,
                ResurrectPuppetSync::encode,
                ResurrectPuppetSync::decode,
                ResurrectPuppetSync::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    private static ResurrectPuppetSync decode(FriendlyByteBuf buf) {
        final int puppetId = buf.readVarInt();
        return new ResurrectPuppetSync(puppetId);
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(puppetId);
    }

    private void handle(Supplier<NetworkEvent.Context> sup) {
        final NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> doWork(ctx));
        ctx.setPacketHandled(true);
    }

    private void doWork(NetworkEvent.Context ctx) {
        NetworkUtil.getValidPuppet(ctx, puppetId).ifPresent(PuppetEntity::resurrect);
    }
}
