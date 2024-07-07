package dev.denimred.simplemuseum.puppet;

import dev.denimred.simplemuseum.init.SMPuppetFacets;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PuppetModel extends GeoModel<Puppet> {
    @Override
    public ResourceLocation getModelResource(Puppet puppet) {
        return puppet.facets().getValue(SMPuppetFacets.MODEL);
    }

    @Override
    public ResourceLocation getTextureResource(Puppet puppet) {
        return puppet.facets().getValue(SMPuppetFacets.TEXTURE);
    }

    @Override
    public ResourceLocation getAnimationResource(Puppet puppet) {
        return puppet.facets().getValue(SMPuppetFacets.ANIMATIONS);
    }
}
