package denimred.simplemuseum.common.network.messages.c2s;

import com.mojang.datafixers.util.Pair;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import denimred.simplemuseum.common.network.NetworkUtil;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

public class ConfigurePuppet {
    private final int puppetId;
    private final List<Pair<PuppetKey, ?>> changes;

    private ConfigurePuppet(int puppetId, List<Pair<PuppetKey, ?>> changes) {
        this.puppetId = puppetId;
        this.changes = changes;
    }

    public static ConfigurePuppet transplant(PuppetEntity source, PuppetEntity target) {
        final List<Pair<PuppetKey, ?>> changes =
                source.getManagers().stream()
                        .flatMap(m -> m.getValues().stream())
                        .filter(
                                v ->
                                        target.getValue(v.provider.key)
                                                .map(tv -> !tv.matches(v))
                                                .orElse(false))
                        .map(v -> Pair.of(v.provider.key, v.get()))
                        .collect(Collectors.toList());
        return new ConfigurePuppet(target.getEntityId(), changes);
    }

    @Deprecated
    public static Builder of(PuppetEntity puppet) {
        return new Builder(puppet.getEntityId());
    }

    public static void register(SimpleChannel channel, int id) {
        channel.registerMessage(
                id,
                ConfigurePuppet.class,
                ConfigurePuppet::encode,
                ConfigurePuppet::decode,
                ConfigurePuppet::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    private static ConfigurePuppet decode(PacketBuffer buf) {
        final int puppetId = buf.readVarInt();
        final int changeCount = buf.readVarInt();
        final List<Pair<PuppetKey, ?>> changes = new ArrayList<>(changeCount);
        for (int i = 0; i < changeCount; i++) {
            final PuppetKey key = new PuppetKey(buf.readString(32767));

            final int size = changes.size();

            PuppetValueProvider.get(key)
                    .map(provider -> provider.serializer)
                    .map(serializer -> serializer.read(buf))
                    .map(change -> Pair.of(key, change))
                    .ifPresent(changes::add);

            if (changes.size() == size) {
                throw new DecoderException(
                        String.format("Failed to decode puppet configuration for '%s'", key));
            }
        }
        return new ConfigurePuppet(puppetId, changes);
    }

    public boolean isEmpty() {
        return changes.isEmpty();
    }

    private void encode(PacketBuffer buf) {
        buf.writeVarInt(puppetId);
        buf.writeVarInt(changes.size());
        for (Pair<PuppetKey, ?> change : changes) {
            final PuppetKey key = change.getFirst();
            final Object value = change.getSecond();

            buf.writeString(key.toString());

            final int writerIndex = buf.writerIndex();

            PuppetValueProvider.get(key)
                    .map(provider -> provider.serializer)
                    .filter(serializer -> serializer.getType().isInstance(value))
                    .ifPresent(serializer -> serializer.write(buf, value));

            if (buf.writerIndex() == writerIndex) {
                throw new EncoderException(
                        String.format(
                                "Failed to encode puppet configuration for '%s' with value '%s'",
                                key, value));
            }
        }
    }

    private void handle(Supplier<NetworkEvent.Context> sup) {
        final NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> doWork(ctx));
        ctx.setPacketHandled(true);
    }

    private void doWork(NetworkEvent.Context ctx) {
        NetworkUtil.getValidPuppet(ctx, puppetId).ifPresent(this::processPuppet);
    }

    private void processPuppet(PuppetEntity puppet) {
        for (Pair<PuppetKey, ?> change : changes) {
            final PuppetKey key = change.getFirst();
            final Object value = change.getSecond();
            puppet.getValue(key).ifPresent(v -> v.trySet(value));
        }
    }

    @Deprecated
    public static class Builder {
        private final int puppetId;
        private final List<Pair<PuppetKey, ?>> changes = new ArrayList<>();

        private Builder(int puppetId) {
            this.puppetId = puppetId;
        }

        public <T> Builder set(PuppetValue<T, ?> value, @Nullable T change) {
            if (change != null) {
                changes.add(Pair.of(value.provider.key, change));
            }
            return this;
        }

        public void send(SimpleChannel channel) {
            if (!changes.isEmpty()) {
                channel.sendToServer(new ConfigurePuppet(puppetId, changes));
            }
        }
    }
}
