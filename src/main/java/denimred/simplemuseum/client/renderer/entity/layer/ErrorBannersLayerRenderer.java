package denimred.simplemuseum.client.renderer.entity.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.OutlineLayerBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.renderer.MuseumRenderType;
import denimred.simplemuseum.common.entity.MuseumDummyEntity;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class ErrorBannersLayerRenderer extends GeoLayerRenderer<MuseumDummyEntity> {
    protected static final ResourceLocation TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/misc/error_banners.png");
    protected static final float TEX_HEIGHT = 0.25F;
    protected final List<Vector3f> vertices;
    protected final int vertCount;

    public ErrorBannersLayerRenderer(IGeoRenderer<MuseumDummyEntity> renderer) {
        super(renderer);
        // Magic numbers ahoy
        final int resolution = 16;
        final double step = Math.PI * 2.0D / resolution;
        final float halfStep = (float) (step / 2.0D);
        final double circumference = step * resolution;
        final double radius = 1.0D;

        // Generate the vertices
        vertices = new ArrayList<>((resolution * 2) + 2);
        for (double radians = 0.0D; radians < circumference; radians += step) {
            final float x = (float) (Math.sin(radians) * radius);
            final float z = (float) (Math.cos(radians) * radius);
            vertices.add(new Vector3f(x, +halfStep, z));
            vertices.add(new Vector3f(x, -halfStep, z));
        }

        // Add the initial vertices to the end to close the loop
        vertices.add(vertices.get(0));
        vertices.add(vertices.get(1));

        // May as well save this for performance I guess ¯\_(ツ)_/¯
        vertCount = vertices.size();
    }

    @Override
    public void render(
            MatrixStack matrixStack,
            IRenderTypeBuffer typeBuffer,
            int light,
            MuseumDummyEntity dummy,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {

        final RenderType type = MuseumRenderType.getErrorBanners(TEXTURE);
        final float time = (float) (ageInTicks * 0.04D);
        final float yPos = (dummy.getHeight() / 2.0F) + 0.25F;

        // Determine the banners that we need to display
        final List<Integer> banners = new ArrayList<>();
        if (dummy.getModelLocation().isInvalid()) {
            banners.add(1);
        }
        if (dummy.getTextureLocation().isInvalid()) {
            banners.add(2);
        }
        if (dummy.getAnimationsLocation().isInvalid() || dummy.getSelectedAnimation().isInvalid()) {
            banners.add(3);
        }

        // Render the banners in the correct position
        final int count = banners.size();
        for (int i = 0; i < count; i++) {
            // Oh my god this is SUCH A MESS
            final float offsetY;
            if (count == 3) {
                if (i == 0) {
                    offsetY = yPos + 0.5F;
                } else if (i == 1) {
                    offsetY = yPos;
                } else {
                    offsetY = yPos - 0.5F;
                }
            } else if (count == 2) {
                offsetY = i == 0 ? yPos + 0.25F : yPos - 0.25F;
            } else {
                offsetY = yPos;
            }
            final float offsetTime = time * (1.0F + (0.5F * i));

            this.renderBanner(banners.get(i), offsetY, offsetTime, matrixStack, typeBuffer, type);
        }
    }

    protected void renderBanner(
            int index,
            float yPos,
            float yAngle,
            MatrixStack matrixStack,
            IRenderTypeBuffer typeBuffer,
            RenderType type) {
        matrixStack.push();
        matrixStack.rotate(new Quaternion(0.0F, yAngle, 0.0F, false));

        // Render the mesh
        final Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        final IVertexBuilder buffer = typeBuffer.getBuffer(type);
        for (int i = 0; i < vertCount; i++) {
            final boolean top = (i & 1) == 0;
            final float u = (top ? i * TEX_HEIGHT - TEX_HEIGHT : (i - 1) * TEX_HEIGHT) / 2;
            final float v = top ? (index - 1) * TEX_HEIGHT : index * TEX_HEIGHT;
            final Vector3f vert = vertices.get(i);
            buffer.pos(matrix4f, vert.getX(), vert.getY() + yPos, vert.getZ())
                    .tex(u, v)
                    .endVertex();
        }

        // Manually draw the buffer to prevent loops from connecting to other loops
        if (typeBuffer instanceof IRenderTypeBuffer.Impl) {
            ((IRenderTypeBuffer.Impl) typeBuffer).finish(type);
        } else if (typeBuffer instanceof OutlineLayerBuffer) {
            ((OutlineLayerBuffer) typeBuffer).buffer.finish(type);
        }

        matrixStack.pop();
    }
}
