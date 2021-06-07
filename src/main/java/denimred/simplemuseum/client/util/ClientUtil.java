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
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import denimred.simplemuseum.client.gui.screen.ConfigureDummyScreen;
import denimred.simplemuseum.client.gui.screen.MuseumDummyScreen;
import denimred.simplemuseum.common.entity.MuseumDummyEntity;
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
    static final Object2ObjectMap<ResourceLocation, AxisAlignedBB>
            MODEL_RENDER_BOUNDS = new Object2ObjectOpenHashMap<>();
    private static final long WINDOW_HANDLE = MC != null ? MC.getMainWindow().getHandle() : 0L;
    private static final Int2LongMap CURSORS = new Int2LongArrayMap(6);
    private static BiFunction<MuseumDummyEntity, Screen, ? extends MuseumDummyScreen>
            lastDummyScreen = ConfigureDummyScreen::new;
    @Nullable private static MuseumDummyEntity selectedDummy;
    private static boolean holdingCane;

    @Nullable
    public static MuseumDummyEntity getHoveredDummy(Entity entity) {
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
                        e -> e instanceof MuseumDummyEntity,
                        range * range);
        return result != null ? (MuseumDummyEntity) result.getEntity() : null;
    }

    public static void setHoldingCane(boolean holdingCane) {
        ClientUtil.holdingCane = holdingCane;
    }

    public static void selectDummy(@Nullable MuseumDummyEntity dummy, boolean changeCursor) {
        if (selectedDummy != dummy) {
            if (changeCursor) {
                if (dummy != null) {
                    ClientUtil.setCursor(GLFW.GLFW_HAND_CURSOR);
                } else {
                    ClientUtil.resetCursor();
                }
            }
            selectedDummy = dummy;
        }
    }

    public static void deselectDummy(boolean changeCursor) {
        selectDummy(null, changeCursor);
    }

    public static boolean shouldDummyGlow(MuseumDummyEntity dummy) {
        return (MC.currentScreen == null || ModCompatUtil.isCryptMasterActive())
                && holdingCane
                && (dummy == selectedDummy || MuseumKeybinds.GLOBAL_HIGHLIGHTS.isKeyDown());
    }

    @Nullable
    public static MuseumDummyEntity getSelectedDummy() {
        return selectedDummy;
    }

    public static void setLastDummyScreen(
            BiFunction<MuseumDummyEntity, Screen, ? extends MuseumDummyScreen> screenBuilder) {
        lastDummyScreen = screenBuilder;
    }

    public static void openDummyScreen(MuseumDummyEntity dummy, @Nullable Screen parent) {
        MC.displayGuiScreen(lastDummyScreen.apply(dummy, parent));
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

    public static AxisAlignedBB getModelBounds(MuseumDummyEntity dummy) {
        final CheckedResource<ResourceLocation> modelLoc = dummy.getModelLocation();
        if (!modelLoc.isInvalid()) {
            final ResourceLocation source = modelLoc.getSafe();
            if (!source.equals(MuseumDummyEntity.DEFAULT_MODEL_LOCATION)) {
                if (MODEL_RENDER_BOUNDS.containsKey(source)) {
                    return MODEL_RENDER_BOUNDS.get(source).offset(dummy.getPositionVec());
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
                                        // Cubes with a 0 on any axis create null quads
                                        .filter(Objects::nonNull)
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
                        return renderBounds.offset(dummy.getPositionVec());
                    }
                }
            }
        }
        return dummy.getBoundingBox();
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
