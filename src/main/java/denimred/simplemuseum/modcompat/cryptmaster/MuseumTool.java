package denimred.simplemuseum.modcompat.cryptmaster;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

import cryptcraft.cryptgui.nodes.INode;
import cryptcraft.cryptgui.nodes.ImageNode;
import cryptcraft.cryptgui.nodes.ImageNodeProps;
import cryptcraft.cryptgui.util.MouseButton;
import cryptcraft.cryptgui.util.MouseButtonState;
import cryptcraft.cryptmaster.plugin.client.IUtilityToolInstance;
import cryptcraft.cryptmaster.plugin.client.ToolCursorState;
import cryptcraft.cryptmaster.plugin.client.UtilityTool;
import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.entity.MuseumDummyEntity;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.messages.c2s.C2SCryptMasterRemoveDummy;
import denimred.simplemuseum.common.network.messages.c2s.C2SCryptMasterSpawnDummy;

public class MuseumTool implements IUtilityToolInstance {
    public static final UtilityTool INSTANCE =
            new UtilityTool(
                    new ResourceLocation(SimpleMuseum.MOD_ID, "main"),
                    new ImageNode(
                            new ImageNodeProps(
                                    new ResourceLocation(
                                            SimpleMuseum.MOD_ID, "textures/item/curators_cane.png"),
                                    null)),
                    MuseumTool::new);

    @Nullable private Vector3d spawnPos;

    private MuseumTool() {
        ClientUtil.setHoldingCane(true);
    }

    @Override
    public void onMouseScrolled(double v) {}

    @Override
    public void render(MatrixStack matrixStack) {}

    @Override
    public void close() {
        ClientUtil.setHoldingCane(false);
        ClientUtil.deselectDummy(true);
        spawnPos = null;
    }

    @Override
    public void onCursorMoved(ToolCursorState cursor) {
        final Entity entity = cursor.raycastToEntity();
        if (entity instanceof MuseumDummyEntity) {
            ClientUtil.selectDummy((MuseumDummyEntity) entity, true);
            spawnPos = null;
        } else {
            ClientUtil.deselectDummy(true);
            spawnPos = cursor.raycastToBlock();
        }
    }

    @Override
    public void onMouseButton(MouseButton button, MouseButtonState state, ToolCursorState cursor) {
        if (state == MouseButtonState.PRESSED) {
            final MuseumDummyEntity dummy = ClientUtil.getSelectedDummy();
            if (button == MouseButton.LEFT) {
                if (dummy != null) {
                    ClientUtil.openDummyScreen(dummy, Minecraft.getInstance().currentScreen);
                } else if (spawnPos != null) {
                    MuseumNetworking.CHANNEL.sendToServer(new C2SCryptMasterSpawnDummy(spawnPos));
                }
            } else if (button == MouseButton.RIGHT && dummy != null) {
                MuseumNetworking.CHANNEL.sendToServer(
                        new C2SCryptMasterRemoveDummy(dummy.getUniqueID()));
            }
        }
    }

    @Nullable
    @Override
    public INode renderSettingsPanel() {
        return null;
    }
}
