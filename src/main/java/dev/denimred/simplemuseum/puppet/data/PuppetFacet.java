package dev.denimred.simplemuseum.puppet.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import dev.denimred.simplemuseum.init.SMPuppetFacets;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static dev.denimred.simplemuseum.SimpleMuseum.LOGGER;

public class PuppetFacet<T> {
    protected final T defaultValue;
    protected final Codec<T> codec;
    protected @Nullable String descriptionId = null;

    public PuppetFacet(T defaultValue, Codec<T> codec) {
        this.defaultValue = defaultValue;
        this.codec = codec.orElse(defaultValue);
    }

    public String getDescriptionId() {
        if (descriptionId == null) descriptionId = Util.makeDescriptionId("puppet_facet", getId());
        return descriptionId;
    }

    public final Codec<T> getCodec() {
        return codec;
    }

    public final T getDefaultValue() {
        return defaultValue;
    }

    public T sanitize(T value) {
        return value;
    }

    public final ResourceLocation getId() {
        return Objects.requireNonNull(SMPuppetFacets.REGISTRY.getKey(this), "Puppet facet not yet registered");
    }

    public void save(CompoundTag namespaceRoot, T value) {
        save(namespaceRoot, value, error -> LOGGER.error("Couldn't serialize puppet facet {}: {}", this, error));
    }

    public void save(CompoundTag namespaceRoot, T value, Consumer<String> onError) {
        if (Objects.equals(value, defaultValue)) return;
        save(value, onError).ifPresent(tag -> namespaceRoot.put(getId().getPath(), tag));
    }

    public Optional<Tag> save(T value, Consumer<String> onError) {
        return codec.encodeStart(NbtOps.INSTANCE, value).resultOrPartial(onError);
    }

    public Optional<T> load(CompoundTag namespaceRoot) {
        return load(namespaceRoot, error -> LOGGER.error("Couldn't deserialize puppet facet {}: {}", this, error));
    }

    public Optional<T> load(CompoundTag namespaceRoot, Consumer<String> onError) {
        var value = namespaceRoot.get(getId().getPath());
        if (value == null) return Optional.empty();
        return load(value, onError);
    }

    public Optional<T> load(Tag valueTag, Consumer<String> onError) {
        return codec.decode(NbtOps.INSTANCE, valueTag).resultOrPartial(onError).map(Pair::getFirst);
    }

    @Override
    public String toString() {
        return Objects.toString(SMPuppetFacets.REGISTRY.getKey(this), "<unknown>");
    }
}
