package denimred.simplemuseum.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.entity.MuseumPuppetEntity;
import denimred.simplemuseum.common.util.CheckedResource;
import denimred.simplemuseum.modcompat.ModCompatUtil;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.exception.GeoModelException;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

// I'm not proud of any of these "emergency fallback" fixes :/
public class MuseumPuppetModel extends AnimatedGeoModel<MuseumPuppetEntity> {
    @Override
    public ResourceLocation getModelLocation(MuseumPuppetEntity entity) {
        return entity.getModelLocation().getSafe();
    }

    @Override
    public GeoModel getModel(ResourceLocation location) {
        try {
            return super.getModel(location);
        } catch (GeoModelException e) {
            // Emergency fallback for when we render while resources are reloading
            SimpleMuseum.LOGGER.debug("EMERGENCY FALLBACK: Model '{}'", location);
            return super.getModel(MuseumPuppetEntity.DEFAULT_MODEL_LOCATION);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(MuseumPuppetEntity entity) {
        final CheckedResource<ResourceLocation> loc = entity.getTextureLocation();
        final ResourceLocation desired = loc.getSafe();
        final TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        if (textureManager.getTexture(desired) != MissingTextureSprite.getDynamicTexture()) {
            return desired;
        } else {
            // Emergency fallback for when we render while resources are reloading
            SimpleMuseum.LOGGER.debug("EMERGENCY FALLBACK: Texture '{}'", desired);
            textureManager.mapTextureObjects.remove(desired);
            return loc.getFallback();
        }
    }

    @Override
    public ResourceLocation getAnimationFileLocation(MuseumPuppetEntity entity) {
        return entity.getAnimationsLocation().getSafe();
    }

    @Nullable // Parent isn't nullable, but its uses say otherwise
    @Override
    public Animation getAnimation(String name, IAnimatable animatable) {
        try {
            return super.getAnimation(name, animatable);
        } catch (NullPointerException e) {
            // Emergency fallback for when we render while resources are reloading
            SimpleMuseum.LOGGER.debug("EMERGENCY FALLBACK: Animation '{}'", name);
            return null;
        }
    }

    @Override
    public void setLivingAnimations(
            MuseumPuppetEntity entity, Integer uniqueID, @Nullable AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);
        if (ModCompatUtil.isCryptMasterPossessing(entity) && customPredicate != null) {
            IBone head = this.getAnimationProcessor().getBone("head");
            if (head == null) {
                head = this.getAnimationProcessor().getBone("Head");
            }
            if (head != null) {
                final EntityModelData extraData =
                        (EntityModelData)
                                customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
                head.setRotationX(
                        head.getRotationX() + (extraData.headPitch * ((float) Math.PI / 180F)));
                head.setRotationY(
                        head.getRotationY() + (extraData.netHeadYaw * ((float) Math.PI / 180F)));
            }
        }
    }
}
