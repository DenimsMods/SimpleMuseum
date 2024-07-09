package dev.denimred.simplemuseum.util;

import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public interface Descriptive {
    default <T> String createDescriptionId(Registry<T> registry, T entry) {
        return Util.makeDescriptionId(registry.key().location().getPath(), registry.getKey(entry));
    }

    default String getDescriptionId(String child) {
        return getDescriptionId() + "." + child;
    }

    default MutableComponent getDisplayName() {
        return Component.translatable(getDescriptionId());
    }

    default MutableComponent getDisplayName(Object... args) {
        return Component.translatable(getDescriptionId(), args);
    }

    default MutableComponent getDisplayName(String child) {
        return Component.translatable(getDescriptionId(child));
    }

    default MutableComponent getDisplayName(String child, Object... args) {
        return Component.translatable(getDescriptionId(child), args);
    }

    String getDescriptionId();
}
