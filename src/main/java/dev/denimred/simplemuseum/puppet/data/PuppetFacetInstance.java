package dev.denimred.simplemuseum.puppet.data;

import net.minecraft.nbt.CompoundTag;

import java.util.function.Consumer;

public final class PuppetFacetInstance<T> {
    private final PuppetFacet<T> facet;
    private final Consumer<PuppetFacetInstance<T>> changeCallback;
    private T value;

    public PuppetFacetInstance(PuppetFacet<T> facet, Consumer<PuppetFacetInstance<T>> changeCallback) {
        this.facet = facet;
        this.changeCallback = changeCallback;
        value = facet.getDefaultValue();
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = facet.sanitize(value);
        changeCallback.accept(this);
    }

    public PuppetFacet<T> getFacet() {
        return facet;
    }

    public void save(CompoundTag root) {
        facet.save(root, value);
    }

    public void load(CompoundTag root) {
        facet.load(root).ifPresent(this::setValue);
    }

    @Override
    public String toString() {
        return facet + " = " + value;
    }
}
