package denimred.simplemuseum.common.network.messages.s2c;

import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;
import java.util.function.Supplier;

import denimred.simplemuseum.common.network.NetworkUtil;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

public final class PlayPuppetAnimation {
    private final int puppetId;
    private final String animation;

    public PlayPuppetAnimation(int puppetId, String animation) {
        this.puppetId = puppetId;
        this.animation = animation;
    }

    public static void register(SimpleChannel channel, int id) {
        channel.registerMessage(
                id,
                PlayPuppetAnimation.class,
                PlayPuppetAnimation::encode,
                PlayPuppetAnimation::decode,
                PlayPuppetAnimation::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    private static PlayPuppetAnimation decode(FriendlyByteBuf buf) {
        final int puppetId = buf.readVarInt();
        final String animation = buf.readUtf();
        return new PlayPuppetAnimation(puppetId, animation);
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(puppetId);
        buf.writeUtf(animation);
    }

    private void handle(Supplier<NetworkEvent.Context> sup) {
        final NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> doWork(ctx));
        ctx.setPacketHandled(true);
    }

    private void doWork(NetworkEvent.Context ctx) {
        NetworkUtil.getValidPuppet(ctx, puppetId)
                .ifPresent(puppet -> puppet.animationManager.playAnimOnce(animation));
    }
}
