package denimred.simplemuseum.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import denimred.simplemuseum.client.gui.screen.MuseumDummyScreen;
import denimred.simplemuseum.common.entity.MuseumDummyEntity;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

public class ClientUtil {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Object2ReferenceMap<String, List<ResourceLocation>> RESOURCE_CACHE =
            new Object2ReferenceOpenHashMap<>();

    public static void openDummyScreen(MuseumDummyEntity dummy) {
        MC.displayGuiScreen(new MuseumDummyScreen(dummy));
    }

    public static Collection<ResourceLocation> getCachedResourceCollection(
            String path, Predicate<ResourceLocation> filter) {
        final IResourceManager manager = MC.getResourceManager();
        return RESOURCE_CACHE.computeIfAbsent(
                path,
                p ->
                        manager.getAllResourceLocations(p, string -> true).stream()
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
                                .collect(Collectors.toList()));
    }

    public static void registerResourceReloadListener() {
        //noinspection ConstantConditions: Minecraft is null during datagen
        if (MC != null) {
            final IReloadableResourceManager resourceManager =
                    (IReloadableResourceManager) MC.getResourceManager();
            resourceManager.addReloadListener(
                    (ISelectiveResourceReloadListener) ClientUtil::onResourceReload);
        }
    }

    private static void onResourceReload(IResourceManager manager, Predicate<IResourceType> types) {
        if (types.test(VanillaResourceType.MODELS) || types.test(VanillaResourceType.TEXTURES)) {
            for (List<ResourceLocation> value : RESOURCE_CACHE.values()) {
                value.clear();
            }
            RESOURCE_CACHE.clear();

            final ClientWorld world = MC.world;
            if (world != null) {
                // This is stupid but it's simple and I'm lazy :)
                for (Entity entity : world.getAllEntities()) {
                    if (entity instanceof MuseumDummyEntity) {
                        ((MuseumDummyEntity) entity).clearAllCached();
                    }
                }
            }
        }
    }
}
