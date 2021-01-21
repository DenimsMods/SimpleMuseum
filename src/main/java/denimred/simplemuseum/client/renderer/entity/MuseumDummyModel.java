package denimred.simplemuseum.client.renderer.entity;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

import denimred.simplemuseum.common.entity.MuseumDummyEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.geo.exception.GeoModelException;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class MuseumDummyModel extends AnimatedGeoModel<MuseumDummyEntity> {
    @Override
    public ResourceLocation getModelLocation(MuseumDummyEntity entity) {
        return entity.getModelLocation().getSafe();
    }

    @Override
    public GeoModel getModel(ResourceLocation location) {
        try {
            return super.getModel(location);
        } catch (GeoModelException e) {
            // Emergency fallback for when we render while resources are reloading
            return super.getModel(MuseumDummyEntity.DEFAULT_MODEL_LOCATION);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(MuseumDummyEntity entity) {
        return entity.getTextureLocation().getSafe();
    }

    @Override
    public ResourceLocation getAnimationFileLocation(MuseumDummyEntity entity) {
        return entity.getAnimationsLocation().getSafe();
    }

    @Nullable // Parent isn't nullable, but its uses say otherwise
    @Override
    public Animation getAnimation(String name, IAnimatable animatable) {
        try {
            return super.getAnimation(name, animatable);
        } catch (NullPointerException e) {
            // Emergency fallback for when we render while resources are reloading
            return null;
        }
    }
}
