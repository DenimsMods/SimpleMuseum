package denimred.simplemuseum.common.network.messages.s2c;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

import denimred.simplemuseum.common.network.NetworkUtil;

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

    private static PlayPuppetAnimation decode(PacketBuffer buf) {
        final int puppetId = buf.readVarInt();
        final String animation = buf.readString();
        return new PlayPuppetAnimation(puppetId, animation);
    }

    private void encode(PacketBuffer buf) {
        buf.writeVarInt(puppetId);
        buf.writeString(animation);
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
