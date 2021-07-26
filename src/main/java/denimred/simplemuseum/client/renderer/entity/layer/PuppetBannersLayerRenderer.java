package denimred.simplemuseum.client.renderer.entity.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.OutlineLayerBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.renderer.MuseumRenderType;
import denimred.simplemuseum.client.renderer.entity.PuppetRenderer;
import denimred.simplemuseum.common.entity.puppet.PuppetEasterEggTracker;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;

import static denimred.simplemuseum.common.entity.puppet.PuppetEasterEggTracker.Egg.ERROR;

public class PuppetBannersLayerRenderer extends GeoLayerRenderer<PuppetEntity> {
    protected static final ResourceLocation TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/misc/puppet_banners.png");
    protected static final int SOURCE_ERROR = 0;
    protected static final int BEHAVIOR_ERROR = 1;
    protected static final int AUDIO_ERROR = 2;
    protected static final int ANIM_ERROR = 3;
    protected static final int RENDER_ERROR = 4;
    protected static final int MISSING_PACK = 5;
    protected static final int DEAD = 6;
    protected static final int HELLO_HOW_R_U = 7;
    protected static final float TEX_HEIGHT = 0.125F;
    protected final List<Vector3f> outerMesh;
    protected final List<Vector3f> innerMesh;
    protected final float meshHeight;
    protected final int vertCount;

    public PuppetBannersLayerRenderer(PuppetRenderer renderer, int resolution, double radius) {
        super(renderer);
        // Generate the meshes (I could technically concat these into one mesh but that's ugly)
        outerMesh = this.generateMesh(false, resolution, radius);
        innerMesh = this.generateMesh(true, resolution, radius);
        // Same as the step value in generateMesh, but whatever
        meshHeight = (float) (Math.PI * 2.0D / resolution);
        // May as well save this for performance I guess ¯\_(ツ)_/¯
        vertCount = outerMesh.size();
    }

    protected static Color getRainbow() {
        final ClientWorld world = Minecraft.getInstance().world;
        return Color.getHSBColor(world == null ? 0.0F : (world.getGameTime() * 0.005F), 1.0F, 1.0F);
    }

    protected List<Vector3f> generateMesh(boolean inner, int resolution, double radius) {
        // Wacky numbers
        final double step = Math.PI * 2.0D / resolution;
        final float halfStep = (float) (step / 2.0D);
        final double circumference = step * resolution;
        // Generate the main loop, reversing the direction if we're generating an inner mesh
        final List<Vector3f> mesh = new ArrayList<>();
        for (double radians = inner ? circumference : 0.0D;
                inner ? (radians >= 0.0D) : (radians < circumference);
                radians += (inner ? -step : step)) {
            final float x = (float) (Math.sin(radians) * radius);
            final float z = (float) (Math.cos(radians) * radius);
            mesh.add(new Vector3f(x, +halfStep, z));
            mesh.add(new Vector3f(x, -halfStep, z));
        }
        // Add the initial vertices to the end to close the loop
        mesh.add(mesh.get(0));
        mesh.add(mesh.get(1));
        return mesh;
    }

    @Override
    public void render(
            MatrixStack matrixStack,
            IRenderTypeBuffer typeBuffer,
            int light,
            PuppetEntity puppet,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        final RenderType type = MuseumRenderType.getErrorBanners(TEXTURE);
        final float time = (float) (ageInTicks * 0.04D);
        final float yPos = (puppet.getHeight() / 2.0F) + 0.25F;
        final boolean doError = puppet.easterEggs.isActive(ERROR);
        if (doError) {
            matrixStack.scale(1.6F, 1.0F, 1.0F);
        }

        // Determine the banners that we need to display
        final List<Integer> banners = new ArrayList<>();
        if (puppet.renderManager.canRenderHiddenDeathEffects()) {
            banners.add(DEAD);
        } else {
            if (!puppet.sourceManager.model.isValid()
                    || !puppet.sourceManager.texture.isValid()
                    || !puppet.sourceManager.animations.isValid()) {
                if (!doError) {
                    banners.add(SOURCE_ERROR);
                }
            }
            if (puppet.sourceManager.animations.isValid()
                    && (!(puppet.animationManager.idle.isValid())
                            || !puppet.animationManager.moving.isValid()
                            || !puppet.animationManager.idleSneak.isValid()
                            || !puppet.animationManager.movingSneak.isValid()
                            || !puppet.animationManager.sprinting.isValid()
                            || !puppet.animationManager.sitting.isValid()
                            || !puppet.animationManager.death.isValid())) {
                banners.add(ANIM_ERROR);
            }
            if (banners.isEmpty()
                    && puppet.easterEggs.isActive(PuppetEasterEggTracker.Egg.HELLO_HOW_R_U)) {
                banners.add(HELLO_HOW_R_U);
            }
        }

        // Render the banners in the correct positions
        final int count = banners.size();
        final float spacing = 0.05F;
        final float height = meshHeight + spacing * 2.0F;
        final float startY = count * height / 2.0F;
        for (int i = 0; i < count; i++) {
            final float yaw =
                    MathHelper.interpolateAngle(
                            partialTicks, puppet.prevRenderYawOffset, puppet.renderYawOffset);
            final float offsetTime = -(time * (1.5F + (0.25F * i))) + (float) Math.toRadians(yaw);

            final float offsetY = startY + spacing - height * (i + 1);
            this.renderBanner(
                    banners.get(i), yPos + offsetY, offsetTime, matrixStack, typeBuffer, type);
        }
    }

    protected void renderBanner(
            int index,
            float yPos,
            float yAngle,
            MatrixStack matrixStack,
            IRenderTypeBuffer typeBuffer,
            RenderType type) {
        matrixStack.push();
        matrixStack.rotate(new Quaternion(0.0F, yAngle, 0.0F, false));

        // Render the two meshes
        final Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        final IVertexBuilder buffer = typeBuffer.getBuffer(type);
        this.renderMesh(index, yPos, matrix4f, buffer, outerMesh);
        this.renderMesh(index, yPos, matrix4f, buffer, innerMesh);

        // Manually draw the buffer to prevent loops from connecting to other loops
        if (typeBuffer instanceof IRenderTypeBuffer.Impl) {
            ((IRenderTypeBuffer.Impl) typeBuffer).finish(type);
        } else if (typeBuffer instanceof OutlineLayerBuffer) {
            ((OutlineLayerBuffer) typeBuffer).buffer.finish(type);
        }

        matrixStack.pop();
    }

    protected void renderMesh(
            int index, float yPos, Matrix4f matrix4f, IVertexBuilder buffer, List<Vector3f> mesh) {
        final Color color = index == HELLO_HOW_R_U ? getRainbow() : Color.WHITE;
        for (int i = 0; i < vertCount; i++) {
            final boolean top = (i & 1) == 0;
            final float u = (top ? i * TEX_HEIGHT - TEX_HEIGHT : (i - 1) * TEX_HEIGHT) / 2;
            final float v = index * TEX_HEIGHT + (top ? 0 : TEX_HEIGHT);
            final Vector3f vert = mesh.get(i);
            buffer.pos(matrix4f, vert.getX(), vert.getY() + yPos, vert.getZ())
                    .color(color.getRed(), color.getGreen(), color.getBlue(), 255)
                    .tex(u, v)
                    .endVertex();
        }
    }
}
