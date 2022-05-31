package denimred.simplemuseum.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static denimred.simplemuseum.client.util.ClientUtil.MC;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static net.minecraft.core.Direction.*;

public class AreaHandler {
    public static AABB area;
    private static Vec3 hitPoint; // wanna use this for some wacky visual effects but I didn't get to that yet
    private static Direction hitFace;
    private static boolean locked;
    private static boolean intersects;
    private static boolean inside;

    public static void raytrace() {
        if(area != null) {
            Camera camera = MC.gameRenderer.getMainCamera();
            Vec3 origin = camera.getPosition();
            Vec3 direction = new Vec3(camera.getLookVector());

            // Points along the ray for the near and far box plane axes
            double minX = (area.minX - origin.x) / direction.x;
            double minY = (area.minY - origin.y) / direction.y;
            double minZ = (area.minZ - origin.z) / direction.z;
            double maxX = (area.maxX - origin.x) / direction.x;
            double maxY = (area.maxY - origin.y) / direction.y;
            double maxZ = (area.maxZ - origin.z) / direction.z;

            // Sort min and max, in case we're looking towards negative
            double nearX = min(minX, maxX);
            double nearY = min(minY, maxY);
            double nearZ = min(minZ, maxZ);
            double farX = max(minX, maxX);
            double farY = max(minY, maxY);
            double farZ = max(minZ, maxZ);

            // Intersection point times along the ray
            double nearT = max(max(nearX, nearY), nearZ);
            double farT = min(min(farX, farY), farZ); // hehe

            intersects = nearT < farT;
            inside = nearT < 0;
            Direction face;

            if (intersects && farT > 0) {
                if (inside) {
                    hitPoint = origin.add(direction.scale(farT));
                    if (farX < farY && farX < farZ) {
                        face = direction.x > 0 ? EAST : WEST;
                    } else if (farY < farZ) {
                        face = direction.y > 0 ? UP : DOWN;
                    } else {
                        face = direction.z > 0 ? SOUTH : NORTH;
                    }
                } else {
                    hitPoint = origin.add(direction.scale(nearT));
                    if (nearX > nearY && nearX > nearZ) {
                        face = direction.x < 0 ? EAST : WEST;
                    } else if (nearY > nearZ) {
                        face = direction.y < 0 ? UP : DOWN;
                    } else {
                        face = direction.z < 0 ? SOUTH : NORTH;
                    }
                }
                if(!locked)
                    hitFace = face;
            } else if(!locked) {
                hitPoint = null;
                hitFace = null;
            }
        }
    }

    public static void render(PoseStack poseStack) {
        MultiBufferSource.BufferSource buffers = MC.renderBuffers().bufferSource();

        poseStack.pushPose();

        Vec3 camPos = MC.gameRenderer.getMainCamera().getPosition();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        // Draw Edges
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, lines, area, 0.0f, intersects ? 1f : 0.5f, 1.0f, 1.0f);

        // Draw Locked Axis
        if(locked) {
            Matrix4f pose = poseStack.last().pose();
            Vec3 center = area.getCenter();
            double length = hitFace.getAxis().choose(area.getXsize(),area.getYsize(), area.getZsize()) / 2;
            Vec3 pos1 = center.add(Vec3.atLowerCornerOf(hitFace.getNormal()).multiply(length - 0.5, length - 0.5, length - 0.5));
            Vec3 pos2 = center.add(Vec3.atLowerCornerOf(hitFace.getNormal()).multiply(length + 1.5, length + 1.5, length + 1.5));

            lines.vertex(pose, (float) pos1.x, (float) pos1.y, (float) pos1.z).color(1f, 0f, 0f, 1f).endVertex();
            lines.vertex(pose, (float) pos2.x, (float) pos2.y, (float) pos2.z).color(1f, 0f, 0f, 1f).endVertex();
        }

        buffers.endBatch(RenderType.lines());

        // Draw Faces
        float x = (float) area.minX;
        float y = (float) area.minY;
        float z = (float) area.minZ;
        float X = (float) area.maxX;
        float Y = (float) area.maxY;
        float Z = (float) area.maxZ;
        VertexConsumer lightning = buffers.getBuffer(RenderType.lightning());
        Matrix4f m4f = poseStack.last().pose();
        for (Direction direction : values()) {
            float r = 0f;
            float g = hitFace == direction ? 0.5f : 0.2f;
            float b = intersects ? 1f : 0.5f;
            float a = hitFace == direction ? 1f : 0.25f;
            // This stuff could be simplified probably...
            switch (direction) {
                case UP:
                    if (inside) {
                        lightning.vertex(m4f, x, Y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, Y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, Y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, x, Y, Z).color(r, g, b, a).endVertex();
                    } else {
                        lightning.vertex(m4f, x, Y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, x, Y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, Y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, Y, z).color(r, g, b, a).endVertex();
                    }
                    break;
                case DOWN:
                    if (inside) {
                        lightning.vertex(m4f, x, y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, x, y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, y, z).color(r, g, b, a).endVertex();
                    } else {
                        lightning.vertex(m4f, x, y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, x, y, Z).color(r, g, b, a).endVertex();
                    }
                    break;
                case NORTH:
                    if (inside) {
                        lightning.vertex(m4f, X, Y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, x, Y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, x, y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, y, z).color(r, g, b, a).endVertex();
                    } else {
                        lightning.vertex(m4f, X, Y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, x, y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, x, Y, z).color(r, g, b, a).endVertex();
                    }
                    break;
                case SOUTH:
                    if (inside) {
                        lightning.vertex(m4f, x, Y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, Y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, x, y, Z).color(r, g, b, a).endVertex();
                    } else {
                        lightning.vertex(m4f, x, Y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, x, y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, Y, Z).color(r, g, b, a).endVertex();
                    }
                    break;
                case WEST:
                    if (inside) {
                        lightning.vertex(m4f, x, Y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, x, Y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, x, y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, x, y, z).color(r, g, b, a).endVertex();
                    } else {
                        lightning.vertex(m4f, x, Y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, x, y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, x, y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, x, Y, Z).color(r, g, b, a).endVertex();
                    }
                    break;
                case EAST:
                    if (inside) {
                        lightning.vertex(m4f, X, Y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, Y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, y, Z).color(r, g, b, a).endVertex();
                    } else {
                        lightning.vertex(m4f, X, Y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, y, Z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, y, z).color(r, g, b, a).endVertex();
                        lightning.vertex(m4f, X, Y, z).color(r, g, b, a).endVertex();
                    }
                    break;
            }
        }
        buffers.endBatch(RenderType.lightning());

        poseStack.popPose();
    }

    public static boolean lock() {
        if(locked)
            locked = false;
        else if(hitFace != null) {
            locked = true;
        }
        return locked;
    }

    public static boolean pushPull(double amount) {
        if (area == null || !intersects || amount == 0) return false;
        if (inside) amount *= -1;

        double x = hitFace.getNormal().getX();
        double y = hitFace.getNormal().getY();
        double z = hitFace.getNormal().getZ();

        AABB newArea;
        if(amount < 0)
            newArea = area.expandTowards(x, y, z);
        else
            newArea = area.contract(x, y, z);

        if(hitFace.getAxis().choose(newArea.getXsize(), newArea.getYsize(), newArea.getZsize()) != 0)
            area = newArea;

        /**
        switch (hitFace) {
            case DOWN:
                area = new AABB(area.minX, area.minY + amount, area.minZ, area.maxX, area.maxY, area.maxZ);
                break;
            case UP:
                area = new AABB(area.minX, area.minY, area.minZ, area.maxX, area.maxY - amount, area.maxZ);
                break;
            case NORTH:
                area = new AABB(area.minX, area.minY, area.minZ + amount, area.maxX, area.maxY, area.maxZ);
                break;
            case SOUTH:
                area = new AABB(area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ - amount);
                break;
            case WEST:
                area = new AABB(area.minX + amount, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ);
                break;
            case EAST:
                area = new AABB(area.minX, area.minY, area.minZ, area.maxX - amount, area.maxY, area.maxZ);
                break;
        }
         **/
        return true;
    }
}