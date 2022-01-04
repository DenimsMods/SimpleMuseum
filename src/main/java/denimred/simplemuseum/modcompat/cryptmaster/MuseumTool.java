package denimred.simplemuseum.modcompat.cryptmaster;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

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
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.messages.c2s.C2SCryptMasterRemovePuppet;
import denimred.simplemuseum.common.network.messages.c2s.C2SCryptMasterSpawnPuppet;

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

    @Nullable private Vec3 spawnPos;

    private MuseumTool() {
        ClientUtil.setHoldingCane(true);
    }

    @Override
    public void onMouseScrolled(double v) {}

    @Override
    public void render(PoseStack matrixStack) {}

    @Override
    public void close() {
        ClientUtil.setHoldingCane(false);
        ClientUtil.deselectPuppet(true);
        spawnPos = null;
    }

    @Override
    public void onCursorMoved(ToolCursorState cursor) {
        final Entity entity = cursor.raycastToEntity();
        if (entity instanceof PuppetEntity) {
            ClientUtil.selectPuppet((PuppetEntity) entity, true);
            spawnPos = null;
        } else {
            ClientUtil.deselectPuppet(true);
            spawnPos = cursor.raycastToBlock();
        }
    }

    @Override
    public void onMouseButton(MouseButton button, MouseButtonState state, ToolCursorState cursor) {
        if (state == MouseButtonState.PRESSED) {
            final PuppetEntity puppet = ClientUtil.getSelectedPuppet();
            if (button == MouseButton.LEFT) {
                if (puppet != null) {
                    ClientUtil.openPuppetScreen(puppet, Minecraft.getInstance().screen);
                } else if (spawnPos != null) {
                    MuseumNetworking.CHANNEL.sendToServer(new C2SCryptMasterSpawnPuppet(spawnPos));
                }
            } else if (button == MouseButton.RIGHT && puppet != null) {
                MuseumNetworking.CHANNEL.sendToServer(
                        new C2SCryptMasterRemovePuppet(puppet.getUUID()));
            }
        }
    }

    @Nullable
    @Override
    public INode renderSettingsPanel() {
        return null;
    }
}
