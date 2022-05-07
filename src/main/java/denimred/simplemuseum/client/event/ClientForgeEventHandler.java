package denimred.simplemuseum.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerChangeGameModeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.MovementEditorClient;
import denimred.simplemuseum.client.util.ClientUtil;
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
            Vec3[] positions = MovementEditorClient.getCurrentMovement().getPositions().toArray(new Vec3[]{});
            MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer buffer = source.getBuffer(RenderType.LINES);
            Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

            event.getMatrixStack().pushPose();
            event.getMatrixStack().translate(-camPos.x, -camPos.y, -camPos.z);

            Matrix4f pose = event.getMatrixStack().last().pose();
            Vec3 lastPos = null;
            for(Vec3 pos : positions) {
                //I don't know what I'm doing with rendering ;-;
                if(lastPos != null) {
                    buffer.vertex(pose, (float) lastPos.x, (float) lastPos.y + 1, (float) lastPos.z).color(1f, 0f, 0f, 1f).endVertex();
                    buffer.vertex(pose, (float) pos.x, (float) pos.y + 1, (float) pos.z).color(1f, 0f, 0f, 1f).endVertex();
                }

                buffer.vertex(pose, (float)pos.x, (float)pos.y, (float)pos.z).color(1f, 0f, 0f, 1f).endVertex();
                buffer.vertex(pose, (float)pos.x, (float)pos.y + 1, (float)pos.z).color(1f, 0f, 0f, 1f).endVertex();

                lastPos = pos;
            }
            source.endBatch(RenderType.LINES);
            event.getMatrixStack().popPose();
        }
    }
}
