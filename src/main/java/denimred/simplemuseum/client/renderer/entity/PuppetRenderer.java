package denimred.simplemuseum.client.renderer.entity;

import static denimred.simplemuseum.common.entity.puppet.PuppetEasterEggTracker.Egg.ERROR;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

import denimred.simplemuseum.client.renderer.entity.layer.PuppetBannersLayerRenderer;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetRenderManager.NameplateBehavior;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.util.RenderUtils;

public class PuppetRenderer extends GeoEntityRenderer<PuppetEntity> {
    public PuppetRenderer(Context context) {
        super(context, new PuppetModel());
        this.addLayer(new PuppetBannersLayerRenderer(this, 16, 1.0D));
    }

    @Override
    public void render(
            GeoModel model,
            PuppetEntity puppet,
            float partialTicks,
            RenderType type,
            PoseStack poseStack,
            @Nullable MultiBufferSource buffers,
            @Nullable VertexConsumer buffer,
            int packedLightIn,
            int packedOverlayIn,
            float red,
            float green,
            float blue,
            float alpha) {
        this.renderEarly(
                puppet,
                poseStack,
                partialTicks,
                buffers,
                buffer,
                packedLightIn,
                packedOverlayIn,
                red,
                green,
                blue,
                alpha);

        if (buffers != null) {
            buffer = buffers.getBuffer(type);
        }
        this.renderLate(
                puppet,
                poseStack,
                partialTicks,
                buffers,
                buffer,
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
                    poseStack,
                    buffer,
                    packedLightIn,
                    packedOverlayIn,
                    red,
                    green,
                    blue,
                    alpha);
        }
        VertexConsumer translucent =
                buffers != null
                        ? buffers.getBuffer(
                                RenderType.entityTranslucent(this.getTextureLocation(puppet)))
                        : buffer;
        // Render all top level bones
        for (GeoBone bone : model.topLevelBones) {
            this.renderRecursivelySlime(
                    bone,
                    poseStack,
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
            PoseStack poseStack,
            @Nullable VertexConsumer buffer,
            int packedLightIn,
            int packedOverlayIn,
            float red,
            float green,
            float blue,
            float alpha) {
        poseStack.pushPose();
        RenderUtils.translate(bone, poseStack);
        RenderUtils.moveToPivot(bone, poseStack);
        RenderUtils.rotate(bone, poseStack);
        RenderUtils.scale(bone, poseStack);
        RenderUtils.moveBackFromPivot(bone, poseStack);

        if (!bone.isHidden) {
            if (!bone.name.endsWith("_slime")) {
                for (GeoCube cube : bone.childCubes) {
                    poseStack.pushPose();
                    renderCube(
                            cube,
                            poseStack,
                            buffer,
                            packedLightIn,
                            packedOverlayIn,
                            red,
                            green,
                            blue,
                            alpha);
                    poseStack.popPose();
                }
            }
            for (GeoBone childBone : bone.childBones) {
                renderRecursivelySolid(
                        childBone,
                        poseStack,
                        buffer,
                        packedLightIn,
                        packedOverlayIn,
                        red,
                        green,
                        blue,
                        alpha);
            }
        }

        poseStack.popPose();
    }

    private void renderRecursivelySlime(
            GeoBone bone,
            PoseStack poseStack,
            @Nullable VertexConsumer buffer,
            int packedLightIn,
            int packedOverlayIn,
            float red,
            float green,
            float blue,
            float alpha) {
        poseStack.pushPose();
        RenderUtils.translate(bone, poseStack);
        RenderUtils.moveToPivot(bone, poseStack);
        RenderUtils.rotate(bone, poseStack);
        RenderUtils.scale(bone, poseStack);
        RenderUtils.moveBackFromPivot(bone, poseStack);

        if (!bone.isHidden) {
            if (bone.name.endsWith("_slime")) {
                for (GeoCube cube : bone.childCubes) {
                    poseStack.pushPose();
                    renderCube(
                            cube,
                            poseStack,
                            buffer,
                            packedLightIn,
                            packedOverlayIn,
                            red,
                            green,
                            blue,
                            alpha);
                    poseStack.popPose();
                }
            }
            for (GeoBone childBone : bone.childBones) {
                renderRecursivelySlime(
                        childBone,
                        poseStack,
                        buffer,
                        packedLightIn,
                        packedOverlayIn,
                        red,
                        green,
                        blue,
                        alpha);
            }
        }

        poseStack.popPose();
    }

    @Override
    public void render(
            PuppetEntity puppet,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLightIn) {
        if (puppet.isCompletelyDead() && !puppet.renderManager.canRenderHiddenDeathEffects()) {
            // Don't render
            return;
        }
        poseStack.pushPose();
        final float scale = puppet.getScale();
        poseStack.scale(scale, scale, scale);
        final int light = !puppet.renderManager.ignoreLighting.get() ? packedLightIn : 0xF000F0;
        super.render(puppet, entityYaw, partialTicks, poseStack, buffers, light);
        poseStack.popPose();
        if (entityRenderDispatcher.shouldRenderHitBoxes()) {
            poseStack.pushPose();
            final AABB aabb = puppet.getBoundingBoxForCulling().move(puppet.position().reverse());
            final VertexConsumer buffer = buffers.getBuffer(RenderType.lines());
            LevelRenderer.renderLineBox(poseStack, buffer, aabb, 0.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        }
    }

    @Override
    protected void applyRotations(
            PuppetEntity puppet,
            PoseStack poseStack,
            float ageInTicks,
            float rotationYaw,
            float partialTicks) {
        super.applyRotations(puppet, poseStack, ageInTicks, rotationYaw, partialTicks);
        if (puppet.easterEggs.isActive(ERROR)) {
            poseStack.scale(0.6F, 1.0F, 1.0F);
        }
    }

    @Override
    public RenderType getRenderType(
            PuppetEntity puppet,
            float partialTicks,
            PoseStack poseStack,
            @Nullable MultiBufferSource buffers,
            @Nullable VertexConsumer buffer,
            int packedLightIn,
            ResourceLocation textureLocation) {
        if (puppet.renderManager.canRenderHiddenDeathEffects()) {
            return RenderType.entityTranslucent(textureLocation);
        }
        return puppet.renderManager.getRenderType(textureLocation);
    }

    @Override
    public Color getRenderColor(
            PuppetEntity puppet,
            float partialTicks,
            PoseStack poseStack,
            @Nullable MultiBufferSource buffers,
            @Nullable VertexConsumer buffer,
            int packedLightIn) {
        if (puppet.easterEggs.isActive(ERROR)) {
            final float b =
                    ((float) Math.sin(puppet.level.getGameTime() / 3.0D) + 1.0F) / 3.34F + 0.4F;
            return Color.ofHSB(0.0F, 1.0F, b);
        } else {
            final Color color = Color.WHITE; // puppet.renderManager.tintColor.get();
            if (puppet.renderManager.canRenderHiddenDeathEffects()) {
                return Color.ofRGBA(
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
    public boolean shouldShowName(PuppetEntity puppet) {
        final NameplateBehavior nameplateBehavior = puppet.renderManager.nameplateBehavior.get();
        return nameplateBehavior != NameplateBehavior.NEVER
                && (super.shouldShowName(puppet) || nameplateBehavior == NameplateBehavior.ALWAYS);
    }
}
