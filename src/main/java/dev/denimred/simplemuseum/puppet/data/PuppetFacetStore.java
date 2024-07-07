package dev.denimred.simplemuseum.puppet.data;

import dev.denimred.simplemuseum.init.SMPuppetFacets;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.nbt.CompoundTag;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class PuppetFacetStore {
    private final Map<PuppetFacet<?>, PuppetFacetInstance<?>> facets = new Object2ReferenceOpenHashMap<>();
    private final Collection<PuppetFacetInstance<?>> instances = facets.values();
    private final Set<PuppetFacetInstance<?>> dirtyInstances = new ReferenceArraySet<>();

    public Collection<PuppetFacetInstance<?>> getAllInstances() {
        return instances;
    }

    public Collection<PuppetFacetInstance<?>> getDirtyInstances() {
        return dirtyInstances;
    }

    @SuppressWarnings("unchecked")
    public <T> PuppetFacetInstance<T> getInstance(PuppetFacet<T> facet) {
        return (PuppetFacetInstance<T>) facets.computeIfAbsent(facet, f -> new PuppetFacetInstance<>(f, dirtyInstances::add));
    }

    public <T> T getValue(PuppetFacet<T> facet) {
        return getInstance(facet).getValue();
    }

    public <T> void setValue(PuppetFacet<T> facet, T newValue) {
        getInstance(facet).setValue(newValue);
    }

    public CompoundTag save() {
        var root = new CompoundTag();
        for (var instance : instances) {
            var key = instance.getFacet().getKey();
            var namespaceRoot = root.getCompound(key.getNamespace());
            instance.save(namespaceRoot);
            if (!namespaceRoot.isEmpty()) root.put(key.getNamespace(), namespaceRoot);
        }
        return root;
    }

    public void load(CompoundTag root) {
        for (PuppetFacet<?> value : SMPuppetFacets.REGISTRY) {
            var key = SMPuppetFacets.REGISTRY.getKey(value);
            assert key != null; // Sanity check; should never fail
            var namespaceRoot = root.getCompound(key.getNamespace());
            loadFrom(value, namespaceRoot);
        }
    }

    private <T> void loadFrom(PuppetFacet<T> facet, CompoundTag namespaceRoot) {
        facet.load(namespaceRoot).ifPresent(value -> setValue(facet, value));
    }
}
