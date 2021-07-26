package denimred.simplemuseum.client.renderer.entity.layer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import java.util.function.Predicate;

import denimred.simplemuseum.client.renderer.entity.PuppetModel;
import denimred.simplemuseum.client.renderer.entity.PuppetRenderer;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;

public class PuppetLayerRenderer extends GeoLayerRenderer<PuppetEntity> {
    protected final PuppetRenderer renderer;
    protected final Predicate<PuppetEntity> renderChecker;

    public PuppetLayerRenderer(
            PuppetRenderer parent,
            ResourceLocation model,
            ResourceLocation texture,
            Predicate<PuppetEntity> renderChecker) {
        super(parent);
        this.renderChecker = renderChecker;
        final PuppetModel provider = (PuppetModel) parent.getGeoModelProvider();
        final PuppetLayerModel layerModel = new PuppetLayerModel(provider, model, texture);
        this.renderer = null; // new PuppetRenderer(parent, layerModel);
    }

    @Override
    public void render(
            MatrixStack matrixStack,
            IRenderTypeBuffer buffer,
            int packedLightIn,
            PuppetEntity puppet,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        if (renderChecker.test(puppet)) {
            matrixStack.rotate(Vector3f.YP.rotationDegrees(180.0F + puppet.rotationYaw));
            renderer.render(
                    puppet, puppet.rotationYaw, limbSwing, matrixStack, buffer, packedLightIn);
        }
    }
}
