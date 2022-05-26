package denimred.simplemuseum.client.event;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerChangeGameModeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.Color;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.MovementEditorClient;
import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.entity.puppet.goals.movement.Movement;
import denimred.simplemuseum.common.entity.puppet.goals.movement.Point;
import denimred.simplemuseum.common.init.MuseumItems;
import denimred.simplemuseum.common.item.CuratorsCaneItem;

@Mod.EventBusSubscriber(
        modid = SimpleMuseum.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public class ClientForgeEventHandler {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() && ClientUtil.MC.screen == null) {
            final Player player = event.player;
            if (player == ClientUtil.MC.player && !player.isSpectator()) {
                final CuratorsCaneItem cane = MuseumItems.CURATORS_CANE.get();
                final boolean holdingCane =
                        player.getMainHandItem().getItem() == cane
                                || player.getOffhandItem().getItem() == cane;
                ClientUtil.setHoldingCane(holdingCane);
                ClientUtil.selectPuppet(
                        holdingCane ? ClientUtil.getHoveredPuppet(player) : null, false);
            }
        }
    }

    @SubscribeEvent
    public static void onClientPlayerChangeGameMode(ClientPlayerChangeGameModeEvent event) {
        if (event.getNewGameMode() == GameType.SPECTATOR) {
            ClientUtil.setHoldingCane(false);
            ClientUtil.deselectPuppet(false);
        }
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        if(MovementEditorClient.isEditing()) {
            Movement movement = MovementEditorClient.getCurrentMovement();

            MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer buffer = source.getBuffer(RenderType.LINES);
            Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

            event.getMatrixStack().pushPose();
            event.getMatrixStack().translate(-camPos.x, -camPos.y, -camPos.z);

            Matrix4f pose = event.getMatrixStack().last().pose();
            Vec3 lastPos = null;

            for(Point point : movement.getMovementPoints()) {
                event.getMatrixStack().pushPose();
                renderMovementPoint(point, lastPos, false, pose, buffer);
                event.getMatrixStack().popPose();
                lastPos = point.pos;
            }

            if(movement instanceof Movement.Area) {
                Vec3 pos = movement.getMovementPoints().get(0).pos;
                buffer.vertex(pose, (float) lastPos.x, (float) lastPos.y + 1, (float) lastPos.z).color(1f, 0f, 0f, 1f).endVertex();
                buffer.vertex(pose, (float) pos.x, (float) pos.y + 1, (float) pos.z).color(1f, 0f, 0f, 1f).endVertex();
                for (Point point : ((Movement.Area) movement).getPointsOfInterest())
                    renderMovementPoint(point, null, true, pose, buffer);
            }

            source.endBatch(RenderType.LINES);
            event.getMatrixStack().popPose();
        }
    }

    private static void renderMovementPoint(Point point, @Nullable Vec3 lastPos, boolean poi, Matrix4f pose, VertexConsumer buffer) {
        Vec3 pos = point.pos;

        if (lastPos != null && !poi) {
            buffer.vertex(pose, (float) lastPos.x, (float) lastPos.y + 1, (float) lastPos.z).color(1f, 0f, 0f, 1f).endVertex();
            buffer.vertex(pose, (float) pos.x, (float) pos.y + 1, (float) pos.z).color(1f, 0f, 0f, 1f).endVertex();
        }
        Color c = poi ? new Color(0f, 0f, 1f) : new Color(1f, 0f, 0f);
        buffer.vertex(pose, (float) pos.x, (float) pos.y, (float) pos.z).color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1f).endVertex();
        buffer.vertex(pose, (float) pos.x, (float) pos.y + 1, (float) pos.z).color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1f).endVertex();
    }

}
