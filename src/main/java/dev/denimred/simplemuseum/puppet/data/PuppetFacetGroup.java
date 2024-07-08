package dev.denimred.simplemuseum.puppet.data;

import com.google.common.collect.ImmutableMap;
import dev.denimred.simplemuseum.init.SMPuppetFacetGroups;
import dev.denimred.simplemuseum.util.Descriptive;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static dev.denimred.simplemuseum.SimpleMuseum.id;

public final class PuppetFacetGroup implements Comparable<PuppetFacetGroup>, Descriptive {
    public static final String ROOT_SECTION = "root";
    private final ImmutableMap<String, List<PuppetFacet<?>>> sections;
    private final Supplier<ItemStack> iconFactory;
    private final int priority;
    private @Nullable ItemStack icon;
    private @Nullable String descriptionId = null;

    private PuppetFacetGroup(Builder builder) {
        sections = ImmutableMap.copyOf(builder.sections);
        iconFactory = builder.iconFactory;
        priority = builder.priority;
    }

    public static Builder builder() {
        return builder(0);
    }

    public static Builder builder(int priority) {
        return new Builder(priority);
    }

    public ItemStack getIcon() {
        if (icon == null) icon = iconFactory.get();
        return icon;
    }

    public ImmutableMap<String, List<PuppetFacet<?>>> getSections() {
        return sections;
    }

    @Override
    public String getDescriptionId() {
        return descriptionId != null ? descriptionId : (descriptionId = createDescriptionId(SMPuppetFacetGroups.REGISTRY, this));
    }

    @Override
    public int compareTo(PuppetFacetGroup o) {
        if (priority != o.priority) return Integer.compare(priority, o.priority);
        var id = SMPuppetFacetGroups.REGISTRY.getKey(this);
        var otherId = SMPuppetFacetGroups.REGISTRY.getKey(o);
        if (id == null) return otherId == null ? 0 : -1;
        if (otherId == null) return 1;
        return id.compareTo(otherId);
    }

    public static final class Builder {
        private final Map<String, List<PuppetFacet<?>>> sections = new Object2ReferenceOpenHashMap<>();
        private final int priority;
        private String currentSection = ROOT_SECTION;
        private Supplier<ItemStack> iconFactory = () -> ItemStack.EMPTY;

        private Builder(int priority) {
            this.priority = priority;
        }

        private List<PuppetFacet<?>> getOrCreateSection(String sectionId) {
            return sections.computeIfAbsent(sectionId, ignored -> new ArrayList<>());
        }

        public Builder section(String sectionId) {
            getOrCreateSection(currentSection = sectionId);
            return this;
        }

        public Builder facet(PuppetFacet<?> facet) {
            getOrCreateSection(currentSection).add(facet);
            return this;
        }

        public Builder icon(ItemLike icon) {
            return icon(() -> new ItemStack(icon));
        }

        public Builder icon(Item icon) {
            return icon(icon::getDefaultInstance);
        }

        public Builder icon(ItemStack icon) {
            return icon(() -> icon);
        }

        public Builder icon(Supplier<ItemStack> iconFactory) {
            this.iconFactory = iconFactory;
            return this;
        }

        public PuppetFacetGroup build() {
            return new PuppetFacetGroup(this);
        }

        @ApiStatus.Internal
        public PuppetFacetGroup register(String id) {
            return Registry.register(SMPuppetFacetGroups.REGISTRY, id(id), build());
        }
    }
}
