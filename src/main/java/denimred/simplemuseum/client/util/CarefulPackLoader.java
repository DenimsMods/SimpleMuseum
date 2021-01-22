package denimred.simplemuseum.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CarefulPackLoader {
    // TODO: Load resources manually to catch each individual edge case
    public static List<ResourceLocation> getAllResources(
            String path, Predicate<ResourceLocation> filter) {
        final IResourceManager manager = Minecraft.getInstance().getResourceManager();
        return manager.getAllResourceLocations(path, string -> true).stream()
                .filter(
                        loc -> {
                            if (!filter.test(loc)) {
                                return false;
                            } else {
                                try {
                                    manager.getResource(loc);
                                    return true;
                                } catch (IOException e) {
                                    return false;
                                }
                            }
                        })
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .collect(Collectors.toList());
    }
}
