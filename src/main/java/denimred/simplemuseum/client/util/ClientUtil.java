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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

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

    public static CompletableFuture<List<ResourceLocation>> getCachedResourcesAsync(
            String path, Predicate<ResourceLocation> filter) {
        if (RESOURCE_CACHE.containsKey(path)) {
            return CompletableFuture.completedFuture(RESOURCE_CACHE.get(path));
        } else {
            return CompletableFuture.supplyAsync(
                    () -> {
                        final List<ResourceLocation> resources =
                                CarefulPackLoader.getAllResources(path, filter);
                        RESOURCE_CACHE.put(path, resources);
                        return resources;
                    });
        }
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
