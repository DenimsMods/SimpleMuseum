package denimred.simplemuseum.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.DatagenModLoader;

import org.lwjgl.glfw.GLFW;

import java.util.function.BiFunction;

import javax.annotation.Nullable;

import denimred.simplemuseum.client.gui.screen.ConfigureDummyScreen;
import denimred.simplemuseum.client.gui.screen.MuseumDummyScreen;
import denimred.simplemuseum.common.entity.MuseumDummyEntity;
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;

import static org.lwjgl.glfw.GLFW.GLFW_ARROW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_VRESIZE_CURSOR;

public class ClientUtil {
    public static final Minecraft MC =
            DatagenModLoader.isRunningDataGen() ? null : Minecraft.getInstance();
    private static final long WINDOW_HANDLE = MC != null ? MC.getMainWindow().getHandle() : 0L;
    private static final Int2LongArrayMap CURSORS = new Int2LongArrayMap(6);
    private static BiFunction<MuseumDummyEntity, Screen, ? extends MuseumDummyScreen>
            lastDummyScreen = ConfigureDummyScreen::new;
    @Nullable
    private static MuseumDummyEntity selectedDummy;

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

    public static void selectDummy(@Nullable MuseumDummyEntity dummy, boolean changeCursor) {
        if (dummy == null) {
            deselectDummy(changeCursor);
        } else if (selectedDummy != dummy) {
            if (changeCursor) {
                ClientUtil.setCursor(GLFW.GLFW_HAND_CURSOR);
            }
            selectedDummy = dummy;
        }
    }

    public static void deselectDummy(boolean changeCursor) {
        if (selectedDummy != null) {
            if (changeCursor) {
                ClientUtil.resetCursor();
            }
            selectedDummy = null;
        }
    }

    public static boolean shouldDummyGlow(MuseumDummyEntity dummy) {
        final Entity renderer = MC.renderViewEntity;
        return dummy == selectedDummy
                && (MC.currentScreen == null || renderer != null && renderer.isSpectator());
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
}
