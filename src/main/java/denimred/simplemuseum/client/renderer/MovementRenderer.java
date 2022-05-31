package denimred.simplemuseum.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.HashMap;

import javax.annotation.Nullable;

import denimred.simplemuseum.client.MovementEditorClient;
import denimred.simplemuseum.client.event.AreaHandler;
import denimred.simplemuseum.common.entity.puppet.goals.movement.Movement;
import denimred.simplemuseum.common.entity.puppet.goals.movement.Point;

public class MovementRenderer {

    public static void render(PoseStack matrixStack) {
        if(MovementEditorClient.isEditing()) {
            Movement movement = MovementEditorClient.getCurrentMovement();

            MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer buffer = source.getBuffer(RenderType.LINES);
            Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

            matrixStack.pushPose();
            matrixStack.translate(-camPos.x, -camPos.y, -camPos.z);

            Matrix4f pose = matrixStack.last().pose();
            Vec3 lastPos = null;

            if(movement instanceof Movement.Path) {
                for (Point point : ((Movement.Path)movement).getMovementPoints()) {
                    matrixStack.pushPose();
                    renderMovementPoint(point, lastPos, false, pose, buffer);
                    matrixStack.popPose();
                    lastPos = point.pos;
                }
            }

            source.endBatch(RenderType.LINES);
            matrixStack.popPose();

            if(movement instanceof Movement.Area && ((Movement.Area)movement).isComplete())
                AreaHandler.render(matrixStack);
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

    private static void renderFace(Matrix4f pose, VertexConsumer buffer, Direction direction, AABB area) {
        Vec3 xyz = new Vec3(area.minX, area.minY, area.minZ);
        Vec3 Xyz = new Vec3(area.maxX, area.minY, area.minZ);
        Vec3 xYz = new Vec3(area.minX, area.maxY, area.minZ);
        Vec3 XYz = new Vec3(area.maxX, area.maxY, area.minZ);
        Vec3 xyZ = new Vec3(area.minX, area.minY, area.maxZ);
        Vec3 XyZ = new Vec3(area.maxX, area.minY, area.maxZ);
        Vec3 xYZ = new Vec3(area.minX, area.maxY, area.maxZ);
        Vec3 XYZ = new Vec3(area.maxX, area.maxY, area.maxZ);

        switch (direction) {
            case NORTH:
                quad(pose, buffer, xYz, XYz, Xyz, xyz);
                break;
            case SOUTH:
                quad(pose, buffer, XYZ, xYZ, xyZ, XyZ);
                break;
            case EAST:
                quad(pose, buffer, XYz, XYZ, XyZ, Xyz);
                break;
            case WEST:
                quad(pose, buffer, xYZ, xYz, xyz, xyZ);
                break;
            case UP:
                quad(pose, buffer, xYZ, XYZ, XYz, xYz);
                break;
            case DOWN:
                quad(pose, buffer, xyz, Xyz, XyZ, xyZ);
                break;
        }
    }

    private static void quad(Matrix4f pose, VertexConsumer buffer, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4) {
        buffer.vertex(pose, (float) p1.x, (float) p1.y, (float) p1.z).color(0f, 0f, 1f, 0.5f).endVertex();
        buffer.vertex(pose, (float) p2.x, (float) p2.y, (float) p2.z).color(0f, 0f, 1f, 0.5f).endVertex();
        buffer.vertex(pose, (float) p3.x, (float) p3.y, (float) p3.z).color(0f, 0f, 1f, 0.5f).endVertex();
        buffer.vertex(pose, (float) p4.x, (float) p4.y, (float) p4.z).color(0f, 0f, 1f, 0.5f).endVertex();
    }

}
