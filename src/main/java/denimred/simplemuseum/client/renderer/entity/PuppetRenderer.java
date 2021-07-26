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
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetRenderManager.NameplateBehavior;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.util.RenderUtils;

import static denimred.simplemuseum.common.entity.puppet.PuppetEasterEggTracker.Egg.ERROR;

public class PuppetRenderer extends GeoEntityRenderer<PuppetEntity> {
    public PuppetRenderer(EntityRendererManager renderManager) {
        super(renderManager, new PuppetModel());
        this.addLayer(new PuppetBannersLayerRenderer(this, 16, 1.0D));
    }

    @Override
    public void render(
            GeoModel model,
            PuppetEntity puppet,
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
            PuppetEntity puppet,
            float entityYaw,
            float partialTicks,
            MatrixStack matrixStack,
            IRenderTypeBuffer typeBuffer,
            int packedLightIn) {
        if (puppet.isCompletelyDead() && !puppet.renderManager.canRenderHiddenDeathEffects()) {
            // Don't render
            return;
        }
        matrixStack.push();
        final float scale = puppet.getRenderScale();
        matrixStack.scale(scale, scale, scale);
        final int light = !puppet.renderManager.ignoreLighting.get() ? packedLightIn : 0xF000F0;
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
    protected void applyRotations(
            PuppetEntity puppet,
            MatrixStack matrixStack,
            float ageInTicks,
            float rotationYaw,
            float partialTicks) {
        super.applyRotations(puppet, matrixStack, ageInTicks, rotationYaw, partialTicks);
        if (puppet.easterEggs.isActive(ERROR)) {
            matrixStack.scale(0.6F, 1.0F, 1.0F);
        }
    }

    @Override
    public ResourceLocation getEntityTexture(PuppetEntity entity) {
        // Uh... what?
        return super.getTextureLocation(entity);
    }

    @Override
    public RenderType getRenderType(
            PuppetEntity puppet,
            float partialTicks,
            MatrixStack stack,
            @Nullable IRenderTypeBuffer renderTypeBuffer,
            @Nullable IVertexBuilder vertexBuilder,
            int packedLightIn,
            ResourceLocation textureLocation) {
        if (puppet.renderManager.canRenderHiddenDeathEffects()) {
            return RenderType.getEntityTranslucent(textureLocation);
        }
        return puppet.renderManager.getRenderType(textureLocation);
    }

    @Override
    public Color getRenderColor(
            PuppetEntity puppet,
            float partialTicks,
            MatrixStack stack,
            @Nullable IRenderTypeBuffer renderTypeBuffer,
            @Nullable IVertexBuilder vertexBuilder,
            int packedLightIn) {
        if (puppet.easterEggs.isActive(ERROR)) {
            final float b =
                    ((float) Math.sin(puppet.world.getGameTime() / 3.0D) + 1.0F) / 3.34F + 0.4F;
            return Color.getHSBColor(0.0F, 1.0F, b);
        } else {
            final Color color = Color.WHITE; // puppet.renderManager.tintColor.get();
            if (puppet.renderManager.canRenderHiddenDeathEffects()) {
                return new Color(
                        color.getRed(),
                        color.getGreen() / 3,
                        color.getBlue() / 3,
                        color.getAlpha() / 4);
            }
            return color;
        }
    }

    @Override
    protected float getDeathMaxRotation(PuppetEntity puppet) {
        return (!puppet.animationManager.death.get().isEmpty()
                                && puppet.animationManager.death.isValid())
                        || puppet.renderManager.canRenderHiddenDeathEffects()
                ? 0.0F
                : 90.0F;
    }

    @Override
    protected boolean canRenderName(PuppetEntity puppet) {
        final NameplateBehavior nameplateBehavior = puppet.renderManager.nameplateBehavior.get();
        return nameplateBehavior != NameplateBehavior.NEVER
                && (super.canRenderName(puppet) || nameplateBehavior == NameplateBehavior.ALWAYS);
    }
}
