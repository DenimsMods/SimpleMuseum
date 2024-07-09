package dev.denimred.simplemuseum.puppet.data;

import com.google.common.collect.*;
import dev.denimred.simplemuseum.init.SMPuppetFacetGroups;
import dev.denimred.simplemuseum.util.Descriptive;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static dev.denimred.simplemuseum.SimpleMuseum.id;

public final class PuppetFacetGroup implements Comparable<PuppetFacetGroup>, Descriptive {
    public static final String ROOT_SECTION = "root";
    public static final Predicate<PuppetFacetGroup> NOT_BLANK = group -> !group.equals(SMPuppetFacetGroups.BLANK);
    private final ImmutableListMultimap<String, PuppetFacet<?>> sections;
    private final Supplier<ItemStack> iconFactory;
    private final int priority;
    private @Nullable ItemStack icon;
    private @Nullable String descriptionId = null;

    private PuppetFacetGroup(Builder builder) {
        sections = ImmutableListMultimap.copyOf(builder.sections);
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

    public boolean hasSections() {
        return !sections.isEmpty() && (sections.size() > 1 || !hasSection(ROOT_SECTION));
    }

    public boolean hasSection(String sectionId) {
        return sections.containsKey(sectionId);
    }

    public boolean hasRootFacets() {
        return !sections.get(ROOT_SECTION).isEmpty();
    }

    public ImmutableMultimap<String, PuppetFacet<?>> getSections() {
        return sections;
    }

    public ImmutableList<PuppetFacet<?>> getSectionFacets(String sectionId) {
        return sections.get(sectionId);
    }

    public String getFirstSectionId() {
        return !hasSections() || hasRootFacets() ? ROOT_SECTION : sections.keys().iterator().next();
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
        return id.compareTo(otherId);
    }

    public static final class Builder {
        private final ListMultimap<String, PuppetFacet<?>> sections = MultimapBuilder.hashKeys().arrayListValues().build();
        private final int priority;
        private String currentSection = ROOT_SECTION;
        private Supplier<ItemStack> iconFactory = () -> ItemStack.EMPTY;

        private Builder(int priority) {
            this.priority = priority;
        }

        public Builder section(String sectionId) {
            currentSection = sectionId;
            return this;
        }

        public Builder facet(PuppetFacet<?> facet) {
            sections.get(currentSection).add(facet);
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

        public PuppetFacetGroup register(ResourceLocation id) {
            return Registry.register(SMPuppetFacetGroups.REGISTRY, id, build());
        }

        @ApiStatus.Internal
        public PuppetFacetGroup register(String id) {
            return Registry.register(SMPuppetFacetGroups.REGISTRY, id(id), build());
        }
    }
}
