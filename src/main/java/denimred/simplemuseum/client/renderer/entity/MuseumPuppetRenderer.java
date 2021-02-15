package denimred.simplemuseum.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;

import denimred.simplemuseum.client.renderer.entity.layer.ErrorBannersLayerRenderer;
import denimred.simplemuseum.common.entity.MuseumPuppetEntity;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class MuseumPuppetRenderer extends GeoEntityRenderer<MuseumPuppetEntity> {
    public MuseumPuppetRenderer(EntityRendererManager renderManager) {
        super(renderManager, new MuseumPuppetModel());
        this.addLayer(new ErrorBannersLayerRenderer(this, 16, 1.0D));
    }

    @Override
    public void render(
            MuseumPuppetEntity puppet,
            float entityYaw,
            float partialTicks,
            MatrixStack matrixStack,
            IRenderTypeBuffer typeBuffer,
            int packedLightIn) {
        super.render(puppet, entityYaw, partialTicks, matrixStack, typeBuffer, packedLightIn);
        if (renderManager.isDebugBoundingBox()) {
            matrixStack.push();
            final AxisAlignedBB aabb =
                    puppet.getRenderBoundingBox().offset(puppet.getPositionVec().inverse());
            final IVertexBuilder buffer = typeBuffer.getBuffer(RenderType.getLines());
            WorldRenderer.drawBoundingBox(matrixStack, buffer, aabb, 0.0F, 1.0F, 1.0F, 1.0F);
            matrixStack.pop();
        }
    }

    @Override
    public RenderType getRenderType(
            MuseumPuppetEntity animatable,
            float partialTicks,
            MatrixStack stack,
            @Nullable IRenderTypeBuffer renderTypeBuffer,
            @Nullable IVertexBuilder vertexBuilder,
            int packedLightIn,
            ResourceLocation textureLocation) {
        return RenderType.getEntityCutoutNoCull(textureLocation);
    }
}
