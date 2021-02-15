package denimred.simplemuseum.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.DatagenModLoader;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import denimred.simplemuseum.client.gui.screen.ConfigurePuppetScreen;
import denimred.simplemuseum.client.gui.screen.MuseumPuppetScreen;
import denimred.simplemuseum.common.entity.MuseumPuppetEntity;
import denimred.simplemuseum.common.init.MuseumKeybinds;
import denimred.simplemuseum.common.util.CheckedResource;
import denimred.simplemuseum.modcompat.ModCompatUtil;
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

import static org.lwjgl.glfw.GLFW.GLFW_ARROW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_VRESIZE_CURSOR;

public class ClientUtil {
    public static final Minecraft MC =
            DatagenModLoader.isRunningDataGen() ? null : Minecraft.getInstance();
    static final Object2ObjectMap<ResourceLocation, AxisAlignedBB> MODEL_RENDER_BOUNDS =
            new Object2ObjectOpenHashMap<>();
    private static final long WINDOW_HANDLE = MC != null ? MC.getMainWindow().getHandle() : 0L;
    private static final Int2LongMap CURSORS = new Int2LongArrayMap(6);
    private static BiFunction<MuseumPuppetEntity, Screen, ? extends MuseumPuppetScreen>
            lastPuppetScreen = ConfigurePuppetScreen::new;
    @Nullable private static MuseumPuppetEntity selectedPuppet;
    private static boolean holdingCane;

    @Nullable
    public static MuseumPuppetEntity getHoveredPuppet(Entity entity) {
        final Vector3d eyes = entity.getEyePosition(1.0F);
        final double range = 100.0D;
        final Vector3d look = entity.getLook(1.0F).scale(range);
        final AxisAlignedBB aabb = entity.getBoundingBox().expand(look).grow(1.0D, 1.0D, 1.0D);
        final EntityRayTraceResult result =
                ProjectileHelper.rayTraceEntities(
                        entity,
                        eyes,
                        eyes.add(look),
                        aabb,
                        e -> e instanceof MuseumPuppetEntity,
                        range * range);
        return result != null ? (MuseumPuppetEntity) result.getEntity() : null;
    }

    public static void setHoldingCane(boolean holdingCane) {
        ClientUtil.holdingCane = holdingCane;
    }

    public static void selectPuppet(@Nullable MuseumPuppetEntity puppet, boolean changeCursor) {
        if (selectedPuppet != puppet) {
            if (changeCursor) {
                if (puppet != null) {
                    ClientUtil.setCursor(GLFW.GLFW_HAND_CURSOR);
                } else {
                    ClientUtil.resetCursor();
                }
            }
            selectedPuppet = puppet;
        }
    }

    public static void deselectPuppet(boolean changeCursor) {
        selectPuppet(null, changeCursor);
    }

    public static boolean shouldPuppetGlow(MuseumPuppetEntity puppet) {
        return (MC.currentScreen == null || ModCompatUtil.isCryptMasterActive())
                && holdingCane
                && (puppet == selectedPuppet || MuseumKeybinds.GLOBAL_HIGHLIGHTS.isKeyDown());
    }

    @Nullable
    public static MuseumPuppetEntity getSelectedPuppet() {
        return selectedPuppet;
    }

    public static void setLastPuppetScreen(
            BiFunction<MuseumPuppetEntity, Screen, ? extends MuseumPuppetScreen> screenBuilder) {
        lastPuppetScreen = screenBuilder;
    }

    public static void openPuppetScreen(MuseumPuppetEntity puppet, @Nullable Screen parent) {
        MC.displayGuiScreen(lastPuppetScreen.apply(puppet, parent));
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

    public static AxisAlignedBB getModelBounds(MuseumPuppetEntity puppet) {
        final CheckedResource<ResourceLocation> modelLoc = puppet.getModelLocation();
        if (!modelLoc.isInvalid()) {
            final ResourceLocation source = modelLoc.getSafe();
            if (!source.equals(MuseumPuppetEntity.DEFAULT_MODEL_LOCATION)) {
                if (MODEL_RENDER_BOUNDS.containsKey(source)) {
                    return MODEL_RENDER_BOUNDS.get(source).offset(puppet.getPositionVec());
                } else {
                    final GeoModel model = GeckoLibCache.getInstance().getGeoModels().get(source);
                    if (model != null) {
                        final Vector3f min =
                                new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
                        final Vector3f max =
                                new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

                        final List<Vector3f> vertices =
                                flattenBones(model.topLevelBones).stream()
                                        .flatMap(bone -> bone.childCubes.stream())
                                        .flatMap(cube -> Arrays.stream(cube.quads))
                                        .flatMap(quad -> Arrays.stream(quad.vertices))
                                        .map(vertex -> vertex.position)
                                        .collect(Collectors.toList());

                        for (final Vector3f vertex : vertices) {
                            final float x = vertex.getX();
                            final float y = vertex.getY();
                            final float z = vertex.getZ();
                            min.setX(Math.min(min.getX(), x));
                            max.setX(Math.max(max.getX(), x));
                            min.setY(Math.min(min.getY(), y));
                            max.setY(Math.max(max.getY(), y));
                            min.setZ(Math.min(min.getZ(), z));
                            max.setZ(Math.max(max.getZ(), z));
                        }

                        final float minAbsX = Math.abs(min.getX());
                        final float maxAbsX = Math.abs(max.getX());
                        final float minAbsZ = Math.abs(min.getZ());
                        final float maxAbsZ = Math.abs(max.getZ());

                        final float longest =
                                Math.max(Math.max(minAbsX, maxAbsX), Math.max(minAbsZ, maxAbsZ));

                        final float minY = min.getY();
                        final float maxY = max.getY();

                        final AxisAlignedBB renderBounds =
                                new AxisAlignedBB(
                                        new Vector3d(-longest, minY, -longest),
                                        new Vector3d(longest, maxY, longest));

                        MODEL_RENDER_BOUNDS.put(source, renderBounds);
                        return renderBounds.offset(puppet.getPositionVec());
                    }
                }
            }
        }
        return puppet.getBoundingBox();
    }

    private static List<GeoBone> flattenBones(List<GeoBone> bones) {
        final List<GeoBone> flat = new ArrayList<>();
        for (GeoBone bone : bones) {
            flat.add(bone);
            flat.addAll(flattenBones(bone.childBones));
        }
        return flat;
    }
}
