package dev.denimred.simplemuseum.puppet.data;

import dev.denimred.simplemuseum.init.SMPuppetFacets;
import dev.denimred.simplemuseum.puppet.entity.Puppet;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static dev.denimred.simplemuseum.SimpleMuseum.LOGGER;
import static dev.denimred.simplemuseum.SimpleMuseum.id;
import static net.minecraft.network.FriendlyByteBuf.DEFAULT_NBT_QUOTA;

public final class SyncPuppetEntityFacets implements FabricPacket {
    public static final PacketType<SyncPuppetEntityFacets> TYPE = PacketType.create(id("sync_puppet_entity_facets"), SyncPuppetEntityFacets::new);
    private final int puppetId;
    private final List<FacetSnapshot<?>> snapshots;

    private SyncPuppetEntityFacets(FriendlyByteBuf buf) {
        puppetId = buf.readVarInt();
        var count = buf.readVarInt();
        snapshots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) snapshots.add(FacetSnapshot.read(buf));
    }

    public SyncPuppetEntityFacets(Puppet puppet, Collection<PuppetFacetInstance<?>> facets) {
        puppetId = puppet.getId();
        snapshots = new ArrayList<>();
        for (var instance : facets) snapshots.add(new FacetSnapshot<>(instance));
    }

    public void handle(Level level) {
        var puppet = Puppet.getPuppet(level, puppetId, "sync puppet entity facets", EnvType.SERVER);
        LOGGER.trace("Syncing puppet #{} facets from server", puppetId);
        for (var snapshot : snapshots) snapshot.apply(puppet.facets());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(puppetId);
        buf.writeVarInt(snapshots.size());
        for (var snapshot : snapshots) snapshot.write(buf);
    }

    @Override
    public PacketType<SyncPuppetEntityFacets> getType() {
        return TYPE;
    }

    private record FacetSnapshot<T>(PuppetFacet<T> facet, T value) {
        FacetSnapshot(PuppetFacetInstance<T> instance) {
            this(instance.getFacet(), instance.getValue());
        }

        static FacetSnapshot<?> read(FriendlyByteBuf buf) {
            var facet = buf.readById(SMPuppetFacets.REGISTRY);
            var tag = readSingleTag(buf);
            return readTyped(Objects.requireNonNull(facet), tag);
        }

        private static <T> FacetSnapshot<T> readTyped(PuppetFacet<T> facet, Tag tag) {
            var value = facet.load(tag, error -> {
                throw new DecoderException(error);
            }).orElseThrow(DecoderException::new);
            return new FacetSnapshot<>(facet, value);
        }

        private static void writeSingleTag(FriendlyByteBuf buf, Tag tag) {
            try {
                NbtIo.writeUnnamedTag(tag, new ByteBufOutputStream(buf));
            } catch (IOException e) {
                throw new EncoderException(e);
            }
        }

        private static Tag readSingleTag(FriendlyByteBuf buf) {
            try {
                return NbtIo.readUnnamedTag(new ByteBufInputStream(buf), 0, new NbtAccounter(DEFAULT_NBT_QUOTA));
            } catch (IOException e) {
                throw new DecoderException(e);
            }
        }

        void write(FriendlyByteBuf buf) {
            buf.writeId(SMPuppetFacets.REGISTRY, facet);
            var tag = facet.save(value, error -> {
                throw new EncoderException(error);
            }).orElseThrow(EncoderException::new);
            writeSingleTag(buf, tag);
        }

        void apply(PuppetFacetStore facets) {
            facets.setValue(facet, value);
        }
    }
}
