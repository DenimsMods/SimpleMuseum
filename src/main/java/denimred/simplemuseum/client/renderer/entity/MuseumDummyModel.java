package denimred.simplemuseum.client.renderer.entity;

import net.minecraft.util.ResourceLocation;

import denimred.simplemuseum.common.entity.MuseumDummyEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class MuseumDummyModel extends AnimatedGeoModel<MuseumDummyEntity> {
    @Override
    public ResourceLocation getModelLocation(MuseumDummyEntity entity) {
        return entity.getModelLocation().getSafe();
    }

    @Override
    public ResourceLocation getTextureLocation(MuseumDummyEntity entity) {
        return entity.getTextureLocation().getSafe();
    }

    @Override
    public ResourceLocation getAnimationFileLocation(MuseumDummyEntity entity) {
        return entity.getAnimationsLocation().getSafe();
    }
}
