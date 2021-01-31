package denimred.simplemuseum.modcompat.cryptmaster;

import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

import cryptcraft.cryptmaster.api.client.IUtilityToolInstance;
import cryptcraft.cryptmaster.api.client.ToolCursorState;
import cryptcraft.cryptmaster.api.client.UtilityTool;
import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.entity.MuseumDummyEntity;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.messages.c2s.C2SCryptMasterRemoveDummy;
import denimred.simplemuseum.common.network.messages.c2s.C2SCryptMasterSpawnDummy;

public class MuseumToolBehavior implements IUtilityToolInstance {
    public static final UtilityTool TOOL_INSTANCE =
            UtilityTool.Companion.withImage(
                    new ResourceLocation(SimpleMuseum.MOD_ID, "main"),
                    new ResourceLocation(SimpleMuseum.MOD_ID, "textures/item/curators_cane.png"),
                    MuseumToolBehavior::new);

    @Nullable private MuseumDummyEntity dummy;
    @Nullable private Vector3d spawnPos;

    @Override
    public void onCursor(ToolCursorState state) {
        final Entity entity = state.raycastToEntity();
        if (entity instanceof MuseumDummyEntity) {
            if (dummy != entity) {
                ClientUtil.setCursor(GLFW.GLFW_HAND_CURSOR);
                dummy = (MuseumDummyEntity) entity;
                dummy.setGlowing(true);
            }
            spawnPos = null;
        } else {
            if (dummy != null) {
                ClientUtil.resetCursor();
                dummy.setGlowing(dummy.isPotionActive(Effects.GLOWING));
                dummy = null;
            }
            spawnPos = state.raycastToBlock();
        }
    }

    @Override
    public void onMouseButton(MouseButton button, ToolCursorState state, boolean pressed) {
        if (pressed) {
            if (button.isLeft()) {
                if (dummy != null) {
                    ClientUtil.resetCursor();
                    ClientUtil.openDummyScreen(dummy, Minecraft.getInstance().currentScreen);
                } else if (spawnPos != null) {
                    MuseumNetworking.CHANNEL.sendToServer(new C2SCryptMasterSpawnDummy(spawnPos));
                }
            } else if (button.isRight() && dummy != null) {
                MuseumNetworking.CHANNEL.sendToServer(
                        new C2SCryptMasterRemoveDummy(dummy.getUniqueID()));
            }
        }
    }

    @Override
    public void onScroll(double v) {}

    @Override
    public void render(MatrixStack matrixStack) {}

    @Override
    public void close() {
        if (dummy != null) {
            ClientUtil.resetCursor();
            dummy.setGlowing(dummy.isPotionActive(Effects.GLOWING));
            dummy = null;
        }
        spawnPos = null;
    }
}
