package denimred.simplemuseum.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.DatagenModLoader;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import denimred.simplemuseum.client.gui.screen.ConfigureDummyScreen;
import denimred.simplemuseum.common.entity.MuseumDummyEntity;
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

import static org.lwjgl.glfw.GLFW.GLFW_ARROW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_VRESIZE_CURSOR;

public class ClientUtil {
    private static final Minecraft MC =
            DatagenModLoader.isRunningDataGen() ? null : Minecraft.getInstance();
    private static final long WINDOW_HANDLE = MC != null ? MC.getMainWindow().getHandle() : 0L;
    private static final Int2LongArrayMap CURSORS = new Int2LongArrayMap(6);
    private static final Object2ReferenceMap<String, List<ResourceLocation>> RESOURCE_CACHE =
            new Object2ReferenceOpenHashMap<>();

    public static void openDummyScreen(MuseumDummyEntity dummy, @Nullable Screen parent) {
        MC.displayGuiScreen(new ConfigureDummyScreen(dummy, parent));
    }

    public static void setCursor(int shape) {
        if (shape >= GLFW_ARROW_CURSOR && shape <= GLFW_VRESIZE_CURSOR) {
            GLFW.glfwSetCursor(
                    WINDOW_HANDLE, CURSORS.computeIfAbsent(shape, GLFW::glfwCreateStandardCursor));
        } else {
            resetCursor();
        }
    }

    public static void resetCursor() {
        GLFW.glfwSetCursor(WINDOW_HANDLE, 0L);
    }

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
