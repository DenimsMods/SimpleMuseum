package denimred.simplemuseum.client.renderer.entity.layer;

import net.minecraft.util.ResourceLocation;

import denimred.simplemuseum.client.renderer.entity.PuppetModel;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PuppetLayerModel extends AnimatedGeoModel<PuppetEntity> {
    private final PuppetModel parent;
    private final ResourceLocation model;
    private final ResourceLocation texture;

    public PuppetLayerModel(PuppetModel parent, ResourceLocation model, ResourceLocation texture) {
        this.parent = parent;
        this.model = model;
        this.texture = texture;
    }

    @Override
    public ResourceLocation getModelLocation(PuppetEntity puppet) {
        return model;
    }

    @Override
    public ResourceLocation getTextureLocation(PuppetEntity puppet) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(PuppetEntity puppet) {
        return parent.getAnimationFileLocation(puppet);
    }
}
