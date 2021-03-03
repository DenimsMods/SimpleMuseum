package denimred.simplemuseum.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import java.awt.Color;

import javax.annotation.Nullable;

import denimred.simplemuseum.client.renderer.entity.layer.PuppetBannersLayerRenderer;
import denimred.simplemuseum.common.entity.MuseumPuppetEntity;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.util.RenderUtils;

// TODO: This is hardcoded to render slime bones as translucent, but it needs to be configurable and
//       expandable (e.g. glowing texture layers, tinted layers, etc.)
public class MuseumPuppetRenderer extends GeoEntityRenderer<MuseumPuppetEntity> {
    public MuseumPuppetRenderer(EntityRendererManager renderManager) {
        super(renderManager, new MuseumPuppetModel());
        this.addLayer(new PuppetBannersLayerRenderer(this, 16, 1.0D));
    }

    @Override
    public void render(
            GeoModel model,
            MuseumPuppetEntity puppet,
            float partialTicks,
            RenderType type,
            MatrixStack matrixStackIn,
            @Nullable IRenderTypeBuffer renderTypeBuffer,
            @Nullable IVertexBuilder vertexBuilder,
            int packedLightIn,
            int packedOverlayIn,
            float red,
            float green,
            float blue,
            float alpha) {
        this.renderEarly(
                puppet,
                matrixStackIn,
                partialTicks,
                renderTypeBuffer,
                vertexBuilder,
                packedLightIn,
                packedOverlayIn,
                red,
                green,
                blue,
                alpha);

        if (renderTypeBuffer != null) {
            vertexBuilder = renderTypeBuffer.getBuffer(type);
        }
        this.renderLate(
                puppet,
                matrixStackIn,
                partialTicks,
                renderTypeBuffer,
                vertexBuilder,
                packedLightIn,
                packedOverlayIn,
                red,
                green,
                blue,
                alpha);
        // Render all top level bones
        for (GeoBone bone : model.topLevelBones) {
            this.renderRecursivelySolid(
                    bone,
                    matrixStackIn,
                    vertexBuilder,
                    packedLightIn,
                    packedOverlayIn,
                    red,
                    green,
                    blue,
                    alpha);
        }
        IVertexBuilder translucent =
                renderTypeBuffer != null
                        ? renderTypeBuffer.getBuffer(
                                RenderType.getEntityTranslucent(this.getTextureLocation(puppet)))
                        : vertexBuilder;
        // Render all top level bones
        for (GeoBone bone : model.topLevelBones) {
            this.renderRecursivelySlime(
                    bone,
                    matrixStackIn,
                    translucent,
                    packedLightIn,
                    packedOverlayIn,
                    red,
                    green,
                    blue,
                    alpha);
        }
    }

    private void renderRecursivelySolid(
            GeoBone bone,
            MatrixStack stack,
            @Nullable IVertexBuilder bufferIn,
            int packedLightIn,
            int packedOverlayIn,
            float red,
            float green,
            float blue,
            float alpha) {
        stack.push();
        RenderUtils.translate(bone, stack);
        RenderUtils.moveToPivot(bone, stack);
        RenderUtils.rotate(bone, stack);
        RenderUtils.scale(bone, stack);
        RenderUtils.moveBackFromPivot(bone, stack);

        if (!bone.isHidden) {
            if (!bone.name.endsWith("_slime")) {
                for (GeoCube cube : bone.childCubes) {
                    stack.push();
                    renderCube(
                            cube,
                            stack,
                            bufferIn,
                            packedLightIn,
                            packedOverlayIn,
                            red,
                            green,
                            blue,
                            alpha);
                    stack.pop();
                }
            }
            for (GeoBone childBone : bone.childBones) {
                renderRecursivelySolid(
                        childBone,
                        stack,
                        bufferIn,
                        packedLightIn,
                        packedOverlayIn,
                        red,
                        green,
                        blue,
                        alpha);
            }
        }

        stack.pop();
    }

    private void renderRecursivelySlime(
            GeoBone bone,
            MatrixStack stack,
            @Nullable IVertexBuilder bufferIn,
            int packedLightIn,
            int packedOverlayIn,
            float red,
            float green,
            float blue,
            float alpha) {
        stack.push();
        RenderUtils.translate(bone, stack);
        RenderUtils.moveToPivot(bone, stack);
        RenderUtils.rotate(bone, stack);
        RenderUtils.scale(bone, stack);
        RenderUtils.moveBackFromPivot(bone, stack);

        if (!bone.isHidden) {
            if (bone.name.endsWith("_slime")) {
                for (GeoCube cube : bone.childCubes) {
                    stack.push();
                    renderCube(
                            cube,
                            stack,
                            bufferIn,
                            packedLightIn,
                            packedOverlayIn,
                            red,
                            green,
                            blue,
                            alpha);
                    stack.pop();
                }
            }
            for (GeoBone childBone : bone.childBones) {
                renderRecursivelySlime(
                        childBone,
                        stack,
                        bufferIn,
                        packedLightIn,
                        packedOverlayIn,
                        red,
                        green,
                        blue,
                        alpha);
            }
        }

        stack.pop();
    }

    @Override
    public void render(
            MuseumPuppetEntity puppet,
            float entityYaw,
            float partialTicks,
            MatrixStack matrixStack,
            IRenderTypeBuffer typeBuffer,
            int packedLightIn) {
        matrixStack.push();
        final float scale = puppet.renderManager.scale.asFloat();
        matrixStack.scale(scale, scale, scale);
        final int light = puppet.renderManager.isLighting() ? packedLightIn : 0xF000F0;
        super.render(puppet, entityYaw, partialTicks, matrixStack, typeBuffer, light);
        matrixStack.pop();
        if (renderManager.isDebugBoundingBox()) {
            matrixStack.push();
            final AxisAlignedBB aabb =
                    puppet.getRenderBoundingBox().offset(puppet.getPositionVec().inverse());
            final IVertexBuilder buffer = typeBuffer.getBuffer(RenderType.getLines());
            WorldRenderer.drawBoundingBox(matrixStack, buffer, aabb, 0.0F, 1.0F, 1.0F, 1.0F);
            matrixStack.pop();
        }
    }

    @Override
    public RenderType getRenderType(
            MuseumPuppetEntity puppet,
            float partialTicks,
            MatrixStack stack,
            @Nullable IRenderTypeBuffer renderTypeBuffer,
            @Nullable IVertexBuilder vertexBuilder,
            int packedLightIn,
            ResourceLocation textureLocation) {
        if (puppet.isDead() && puppet.isInvisible()) {
            return RenderType.getEntityTranslucent(textureLocation);
        }
        return puppet.renderManager.getRenderType(textureLocation);
    }

    @Override
    public Color getRenderColor(
            MuseumPuppetEntity puppet,
            float partialTicks,
            MatrixStack stack,
            @Nullable IRenderTypeBuffer renderTypeBuffer,
            @Nullable IVertexBuilder vertexBuilder,
            int packedLightIn) {
        Color color = puppet.renderManager.getColor();
        if (puppet.isDead() && puppet.isInvisible()) {
            color =
                    new Color(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            color.getAlpha() / 4);
        }
        return color;
    }

    @Override
    protected float getDeathMaxRotation(MuseumPuppetEntity puppet) {
        return puppet.animationManager.hasDeathAnim() ? 0.0F : 90.0F;
    }
}
