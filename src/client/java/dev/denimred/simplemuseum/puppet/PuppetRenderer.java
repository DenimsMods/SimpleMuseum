package dev.denimred.simplemuseum.puppet;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PuppetRenderer extends GeoEntityRenderer<Puppet> {
    public PuppetRenderer(Context context) {
        super(context, new PuppetModel());
    }
}
