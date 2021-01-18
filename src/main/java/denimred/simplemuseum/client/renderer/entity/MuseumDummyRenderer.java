package denimred.simplemuseum.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererManager;

import denimred.simplemuseum.common.entity.MuseumDummyEntity;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class MuseumDummyRenderer extends GeoEntityRenderer<MuseumDummyEntity> {
    public MuseumDummyRenderer(EntityRendererManager renderManager) {
        super(renderManager, new MuseumDummyModel());
    }
}
