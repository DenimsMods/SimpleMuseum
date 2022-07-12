package denimred.simplemuseum.client.util;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetAnimationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

public class ResourceUtil {
    private static final Object2ReferenceMap<String, List<ResourceLocation>> RESOURCE_CACHE =
            new Object2ReferenceOpenHashMap<>();

    // TODO: Load resources manually to catch each individual edge case
    public static List<ResourceLocation> getAllResources(
            String path, Predicate<ResourceLocation> filter) {
        final ResourceManager manager = Minecraft.getInstance().getResourceManager();
        return manager.listResources(path, string -> true).stream()
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

    public static CompletableFuture<List<ResourceLocation>> getCachedResourcesAsync(
            String path, Predicate<ResourceLocation> filter) {
        if (RESOURCE_CACHE.containsKey(path)) {
            return CompletableFuture.completedFuture(RESOURCE_CACHE.get(path));
        } else {
            return CompletableFuture.supplyAsync(
                    () -> {
                        final List<ResourceLocation> resources = getAllResources(path, filter);
                        RESOURCE_CACHE.put(path, resources);
                        return resources;
                    });
        }
    }

    public static void registerResourceReloadListener() {
        if (ClientUtil.MC != null) {
            final ReloadableResourceManager manager =
                    (ReloadableResourceManager) ClientUtil.MC.getResourceManager();
            manager.registerReloadListener(
                    (ISelectiveResourceReloadListener) ResourceUtil::onResourceReload);
        }
    }

    private static void onResourceReload(ResourceManager manager, Predicate<IResourceType> types) {
        if (types.test(VanillaResourceType.MODELS) || types.test(VanillaResourceType.TEXTURES)) {
            for (List<ResourceLocation> value : RESOURCE_CACHE.values()) {
                value.clear();
            }

            PuppetAnimationManager.EXPRESSION_DATA_CACHE.clear();
            RESOURCE_CACHE.clear();
            ClientUtil.MODEL_BOUNDS.clear();

            final ClientLevel level = ClientUtil.MC.level;
            if (level != null) {
                // This is stupid but it's simple and I'm lazy :)
                for (Entity entity : level.entitiesForRendering()) {
                    if (entity instanceof PuppetEntity) {
                        ((PuppetEntity) entity).invalidateCaches();
                    }
                }
            }
        }
    }
}
