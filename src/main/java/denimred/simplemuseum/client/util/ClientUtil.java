package denimred.simplemuseum.client.util;

import static org.lwjgl.glfw.GLFW.GLFW_ARROW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_VRESIZE_CURSOR;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DatagenModLoader;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.gui.screen.ItemManagementScreen;
import denimred.simplemuseum.client.gui.screen.PuppetConfigScreen;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetRenderManager;
import denimred.simplemuseum.common.init.MuseumKeybinds;
import denimred.simplemuseum.modcompat.ModCompat;
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class ClientUtil {
    public static final Minecraft MC =
            DatagenModLoader.isRunningDataGen() ? null : Minecraft.getInstance();
    static final Object2ReferenceMap<ResourceLocation, Pair<EntityDimensions, AABB>> MODEL_BOUNDS =
            new Object2ReferenceOpenHashMap<>();
    private static final long WINDOW_HANDLE = MC != null ? MC.getWindow().getWindow() : 0L;
    private static final Int2LongMap CURSORS = new Int2LongArrayMap(6);
    @Nullable private static PuppetEntity selectedPuppet;
    private static boolean holdingCane;

    @Nullable
    public static PuppetEntity getHoveredPuppet(Entity entity) {
        final Vec3 eyes = entity.getEyePosition(1.0F);
        final double range = 100.0D;
        final Vec3 look = entity.getViewVector(1.0F).scale(range);
        final AABB aabb = entity.getBoundingBox().expandTowards(look).inflate(1.0D, 1.0D, 1.0D);
        final EntityHitResult result =
                ProjectileUtil.getEntityHitResult(
                        entity,
                        eyes,
                        eyes.add(look),
                        aabb,
                        e -> e instanceof PuppetEntity,
                        range * range);
        return result != null ? (PuppetEntity) result.getEntity() : null;
    }

    public static boolean isHoldingCane() {
        return holdingCane;
    }

    public static void setHoldingCane(boolean holdingCane) {
        ClientUtil.holdingCane = holdingCane;
    }

    public static void selectPuppet(@Nullable PuppetEntity puppet, boolean changeCursor) {
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

    public static boolean shouldPuppetGlow(PuppetEntity puppet) {
        return puppet.renderManager.canRenderHiddenDeathEffects()
                || (MC.screen == null || ModCompat.CryptMaster.isActive())
                        && holdingCane
                        && (puppet == selectedPuppet || MuseumKeybinds.GLOBAL_HIGHLIGHTS.isDown());
    }

    @Nullable
    public static PuppetEntity getSelectedPuppet() {
        return selectedPuppet;
    }

    public static void openPuppetScreen(PuppetEntity puppet, @Nullable Screen parent) {
        MC.setScreen(new PuppetConfigScreen(puppet, parent));
    }

    public static void openPuppetItemScreen(PuppetEntity puppet, @Nullable Screen parent) {
        MC.setScreen(new ItemManagementScreen(puppet, parent));
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

    public static Optional<Pair<EntityDimensions, AABB>> getPuppetBounds(PuppetEntity puppet) {
        final ResourceLocation source = puppet.sourceManager.model.get();
        if (MODEL_BOUNDS.containsKey(source)) {
            return Optional.of(MODEL_BOUNDS.get(source));
        } else {
            try {
                return Optional.of(
                        MODEL_BOUNDS.computeIfAbsent(
                                puppet.sourceManager.model.getSafe(),
                                ClientUtil::generateModelBounds));
            } catch (IllegalArgumentException e) {
                // This is usually impossible, but can theoretically happen
                // TODO: Test for edge/corner cases
                SimpleMuseum.LOGGER.warn("Failed to generate model bounds", e);
                return Optional.empty();
            }
        }
    }

    public static Pair<EntityDimensions, AABB> generateModelBounds(ResourceLocation source) {
        final GeoModel model = GeckoLibCache.getInstance().getGeoModels().get(source);
        if (model == null) {
            throw new IllegalArgumentException("Invalid model file " + source);
        }

        // Collect every vertex position from the model
        final List<Vector3f> vertices =
                flattenBones(model.topLevelBones).stream()
                        .flatMap(bone -> bone.childCubes.stream())
                        .flatMap(cube -> Arrays.stream(cube.quads))
                        // Cubes with a 0 on any axis create null quads
                        .filter(Objects::nonNull)
                        .flatMap(quad -> Arrays.stream(quad.vertices))
                        .map(vertex -> vertex.position)
                        .collect(Collectors.toList());

        // Min/max start with extreme opposite values, to be refined later
        final Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        final Vector3f max = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

        // Refine min/max towards points that are furthest from the model's center
        for (final Vector3f vertex : vertices) {
            final float x = vertex.x();
            final float y = vertex.y();
            final float z = vertex.z();
            min.setX(Math.min(min.x(), x));
            max.setX(Math.max(max.x(), x));
            min.setY(Math.min(min.y(), y));
            max.setY(Math.max(max.y(), y));
            min.setZ(Math.min(min.z(), z));
            max.setZ(Math.max(max.z(), z));
        }

        // Calculate the longest and shortest horizontal edges using the min/max absolute values
        final float minAbsX = Math.abs(min.x());
        final float maxAbsX = Math.abs(max.x());
        final float minAbsZ = Math.abs(min.z());
        final float maxAbsZ = Math.abs(max.z());
        final float shortest = Math.min(Math.min(minAbsX, maxAbsX), Math.min(minAbsZ, maxAbsZ));
        final float longest = Math.max(Math.max(minAbsX, maxAbsX), Math.max(minAbsZ, maxAbsZ));

        // We don't factor the vertical edges into the edge length calculations
        final float minY = min.y();
        final float maxY = max.y();

        // Collision bounds are defined by the shortest edge, render bounds by the longest
        // TODO: Clamp the collision bounds to a minimum and maximum size
        final EntityDimensions collisionBounds = EntityDimensions.scalable(shortest * 2, maxY);
        final AABB renderBounds =
                new AABB(new Vec3(-longest, minY, -longest), new Vec3(longest, maxY, longest));

        return Pair.of(collisionBounds, renderBounds);
    }

    public static List<GeoBone> flattenBones(List<GeoBone> bones) {
        final List<GeoBone> flat = new ArrayList<>();
        for (GeoBone bone : bones) {
            flat.add(bone);
            flat.addAll(flattenBones(bone.childBones));
        }
        return flat;
    }

    /** Plays a given sound by name. Primarily exists to suppress {@linkplain OnlyIn} exceptions. */
    public static void playArbitrarySound(
            ResourceLocation soundName, SoundSource source, Vec3 pos, float volume, float pitch) {
        final SimpleSoundInstance instance =
                new SimpleSoundInstance(
                        soundName,
                        source,
                        volume,
                        pitch,
                        false,
                        0,
                        SoundInstance.Attenuation.LINEAR,
                        pos.x,
                        pos.y,
                        pos.z,
                        false);
        MC.getSoundManager().play(instance);
    }

    /** Returns true if the keybind is pressed, regardless of the current screen or input mode. */
    public static boolean hasKeyDown(KeyMapping mapping) {
        return InputConstants.isKeyDown(WINDOW_HANDLE, mapping.getKey().getValue());
    }

    public static RenderType typeFromLayer(
            PuppetRenderManager.RenderLayer layer, ResourceLocation texture) {
        switch (layer) {
            case CUTOUT:
                return RenderType.entityCutoutNoCull(texture);
            case CUTOUT_CULL:
                return RenderType.entityCutout(texture);
            case TRANSLUCENT:
                return RenderType.entityTranslucent(texture);
            case TRANSLUCENT_CULL:
                return RenderType.entityTranslucentCull(texture);
            default:
                throw new IllegalArgumentException("Unexpected value: " + layer);
        }
    }

    public static boolean isClientPossessing(PuppetEntity puppet) {
        final Entity possessor = puppet.getPossessor();
        return possessor != null && possessor.equals(MC.player);
    }
}
