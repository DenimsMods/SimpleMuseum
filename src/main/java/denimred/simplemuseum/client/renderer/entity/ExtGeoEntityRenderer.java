package denimred.simplemuseum.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.awt.Color;
import java.util.Collections;

import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;

public abstract class ExtGeoEntityRenderer<T extends LivingEntity & IAnimatable>
        extends GeoEntityRenderer<T> {
    protected static final Minecraft MC = Minecraft.getInstance();
    protected final AnimatedGeoModel<T> modelProvider;

    protected ExtGeoEntityRenderer(
            EntityRendererManager manager, AnimatedGeoModel<T> modelProvider) {
        super(manager, modelProvider);
        this.modelProvider = modelProvider;
    }

    @Override
    public ResourceLocation getEntityTexture(T entity) {
        // Mappings nonsense...
        return this.getTextureLocation(entity);
    }

    @Override
    public void render(
            T entity,
            float yaw,
            float partialTicks,
            MatrixStack stack,
            IRenderTypeBuffer typeBuffer,
            int light) {
        stack.push();
        final Entity ridingEntity = entity.getRidingEntity();
        final boolean isSitting =
                entity.isPassenger() && (ridingEntity != null && ridingEntity.shouldRiderSit());
        final EntityModelData modelData = new EntityModelData();
        modelData.isSitting = isSitting;
        final boolean isChild = entity.isChild();
        modelData.isChild = isChild;

        float renderYaw =
                MathHelper.rotLerp(
                        partialTicks, entity.prevRenderYawOffset, entity.renderYawOffset);
        float headYaw =
                MathHelper.rotLerp(
                        partialTicks, entity.prevRotationYawHead, entity.rotationYawHead);
        float netHeadYaw = headYaw - renderYaw;
        if (isSitting && ridingEntity instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity) ridingEntity;
            renderYaw =
                    MathHelper.rotLerp(
                            partialTicks,
                            livingentity.prevRenderYawOffset,
                            livingentity.renderYawOffset);
            netHeadYaw = headYaw - renderYaw;
            float f3 = MathHelper.wrapDegrees(netHeadYaw);
            if (f3 < -85.0F) {
                f3 = -85.0F;
            }

            if (f3 >= 85.0F) {
                f3 = 85.0F;
            }

            renderYaw = headYaw - f3;
            if (f3 * f3 > 2500.0F) {
                renderYaw += f3 * 0.2F;
            }

            netHeadYaw = headYaw - renderYaw;
        }

        float headPitch =
                MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch);
        if (entity.getPose() == Pose.SLEEPING) {
            Direction direction = entity.getBedDirection();
            if (direction != null) {
                float f4 = entity.getEyeHeight(Pose.STANDING) - 0.1F;
                stack.translate(
                        (float) (-direction.getXOffset()) * f4,
                        0.0D,
                        (float) (-direction.getZOffset()) * f4);
            }
        }
        float f7 = this.handleRotationFloat(entity, partialTicks);
        this.applyRotations(entity, stack, f7, renderYaw, partialTicks);

        float limbSwingAmount = 0.0F;
        float limbSwing = 0.0F;
        if (!isSitting && entity.isAlive()) {
            limbSwingAmount =
                    MathHelper.lerp(
                            partialTicks, entity.prevLimbSwingAmount, entity.limbSwingAmount);
            limbSwing = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);
            if (isChild) {
                limbSwing *= 3.0F;
            }

            if (limbSwingAmount > 1.0F) {
                limbSwingAmount = 1.0F;
            }
        }
        modelData.headPitch = -headPitch;
        modelData.netHeadYaw = -netHeadYaw;

        AnimationEvent<T> predicate =
                new AnimationEvent<>(
                        entity,
                        limbSwing,
                        limbSwingAmount,
                        partialTicks,
                        !(limbSwingAmount > -0.15F && limbSwingAmount < 0.15F),
                        Collections.singletonList(modelData));
        GeoModel model = modelProvider.getModel(modelProvider.getModelLocation(entity));
        modelProvider.setLivingAnimations(entity, this.getUniqueID(entity), predicate);

        stack.translate(0, 0.01f, 0);
        final Color color =
                this.getRenderColor(entity, partialTicks, stack, typeBuffer, null, light);
        RenderType renderType =
                getRenderType(
                        entity,
                        partialTicks,
                        stack,
                        typeBuffer,
                        null,
                        light,
                        getTextureLocation(entity));
        final boolean invisible = MC.player != null && entity.isInvisibleToPlayer(MC.player);
        render(
                model,
                entity,
                partialTicks,
                renderType,
                stack,
                typeBuffer,
                null,
                light,
                getPackedOverlay(entity, 0),
                (float) color.getRed() / 255f,
                (float) color.getGreen() / 255f,
                (float) color.getBlue() / 255f,
                invisible ? 0.0F : (float) color.getAlpha() / 255);

        if (!entity.isSpectator()) {
            for (GeoLayerRenderer<T> layerRenderer : this.layerRenderers) {
                layerRenderer.render(
                        stack,
                        typeBuffer,
                        light,
                        entity,
                        limbSwing,
                        limbSwingAmount,
                        partialTicks,
                        f7,
                        netHeadYaw,
                        headPitch);
            }
        }
        stack.pop();

        RenderNameplateEvent event =
                new RenderNameplateEvent(
                        entity,
                        entity.getDisplayName(),
                        this,
                        stack,
                        typeBuffer,
                        light,
                        partialTicks);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() != Event.Result.DENY
                && (event.getResult() == Event.Result.ALLOW || this.canRenderName(entity))) {
            this.renderName(entity, event.getContent(), stack, typeBuffer, light);
        }
    }
}
