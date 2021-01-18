package denimred.simplemuseum.client.renderer.entity;

import net.minecraft.util.ResourceLocation;

import denimred.simplemuseum.common.entity.MuseumDummyEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class MuseumDummyModel extends AnimatedGeoModel<MuseumDummyEntity> {
    @Override
    public ResourceLocation getModelLocation(MuseumDummyEntity entity) {
        return entity.getModel().getSafe();
    }

    @Override
    public ResourceLocation getTextureLocation(MuseumDummyEntity entity) {
        return entity.getTexture().getSafe();
    }

    @Override
    public ResourceLocation getAnimationFileLocation(MuseumDummyEntity entity) {
        return entity.getAnimations().getSafe();
    }
}
