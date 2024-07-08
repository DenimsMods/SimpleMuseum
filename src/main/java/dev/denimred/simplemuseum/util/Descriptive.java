package dev.denimred.simplemuseum.util;

import net.minecraft.Util;
import net.minecraft.core.Registry;

public interface Descriptive {
    default <T> String createDescriptionId(Registry<T> registry, T entry) {
        return Util.makeDescriptionId(registry.key().location().getPath(), registry.getKey(entry));
    }

    default String getDescriptionId(String child) {
        return getDescriptionId() + "." + child;
    }

    String getDescriptionId();
}
