package denimred.simplemuseum.client.renderer.entity;

import static denimred.simplemuseum.common.entity.puppet.PuppetEasterEggTracker.Egg.ERROR;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import denimred.simplemuseum.client.resources.data.ExpressionData;
import denimred.simplemuseum.client.resources.data.ExpressionDataSection;
import denimred.simplemuseum.client.resources.data.ExpressionFaceData;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetAnimationManager;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

import denimred.simplemuseum.client.renderer.entity.layer.PuppetBannersLayerRenderer;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetRenderManager.NameplateBehavior;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.*;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.util.RenderUtils;

public class PuppetRenderer extends GeoEntityRenderer<PuppetEntity> {
    public PuppetRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher, new PuppetModel());
        this.addLayer(new PuppetBannersLayerRenderer(this, 16, 1.0D));
    }

    @Override
    public void render(GeoModel model, PuppetEntity puppet, float partialTicks, RenderType type, PoseStack poseStack, @Nullable MultiBufferSource buffers, @Nullable VertexConsumer buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        this.renderEarly(puppet, poseStack, partialTicks, buffers, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        if (buffers != null) {
            buffer = buffers.getBuffer(type);
        }
        this.renderLate(puppet, poseStack, partialTicks, buffers, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        // Render all top level bones
        for (GeoBone bone : model.topLevelBones) {
            this.renderRecursivelySolid(puppet, bone, poseStack, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }
        VertexConsumer translucent = buffers != null ? buffers.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(puppet))) : buffer;
        // Render all top level bones
        for (GeoBone bone : model.topLevelBones) {
            this.renderRecursivelySlime(puppet, bone, poseStack, translucent, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }
    }

    private void renderRecursivelySolid(PuppetEntity puppet, GeoBone bone, PoseStack poseStack, @Nullable VertexConsumer buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        RenderUtils.translate(bone, poseStack);
        RenderUtils.moveToPivot(bone, poseStack);
        RenderUtils.rotate(bone, poseStack);
        RenderUtils.scale(bone, poseStack);
        RenderUtils.moveBackFromPivot(bone, poseStack);

        if (!bone.isHidden) {
            if (!bone.name.endsWith("_slime")) {
                int i = 0;
                for (GeoCube cube : bone.childCubes) {
                    poseStack.pushPose();
                    renderCube(puppet, bone, cube, i, poseStack, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                    poseStack.popPose();
                    i++;
                }
            }
            for (GeoBone childBone : bone.childBones) {
                renderRecursivelySolid(puppet, childBone, poseStack, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            }
        }

        poseStack.popPose();
    }

    private void renderRecursivelySlime(PuppetEntity puppet, GeoBone bone, PoseStack poseStack, @Nullable VertexConsumer buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        RenderUtils.translate(bone, poseStack);
        RenderUtils.moveToPivot(bone, poseStack);
        RenderUtils.rotate(bone, poseStack);
        RenderUtils.scale(bone, poseStack);
        RenderUtils.moveBackFromPivot(bone, poseStack);

        if (!bone.isHidden) {
            if (bone.name.endsWith("_slime")) {
                int i = 0;
                for (GeoCube cube : bone.childCubes) {
                    poseStack.pushPose();
                    renderCube(puppet, bone, cube, i, poseStack, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                    poseStack.popPose();
                    i++;
                }
            }
            for (GeoBone childBone : bone.childBones) {
                renderRecursivelySlime(puppet, childBone, poseStack, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            }
        }

        poseStack.popPose();
    }

    public void renderCube(PuppetEntity puppet, GeoBone bone, GeoCube cube, int cubeID, PoseStack stack, VertexConsumer bufferIn, int packedLightIn,
                            int packedOverlayIn, float red, float green, float blue, float alpha) {
        RenderUtils.moveToPivot(cube, stack);
        RenderUtils.rotate(cube, stack);
        RenderUtils.moveBackFromPivot(cube, stack);
        Matrix3f matrix3f = stack.last().normal();
        Matrix4f matrix4f = stack.last().pose();

        boolean isExpressionValid =
                puppet.animationManager.expressionsEnabled.get() &&
                (puppet.animationManager.expression.isValid() || !puppet.animationManager.animatedExpression.equals(""));
        String currExpression = puppet.animationManager.expression.isValid() ? puppet.animationManager.expression.get() : puppet.animationManager.animatedExpression;
        ExpressionDataSection expressionDataSection = isExpressionValid ?
                PuppetAnimationManager.getExpressionData(puppet.sourceManager.model.getSafe()) : // Retrieve expression section from puppet's animation manager
                ExpressionDataSection.EMPTY;
        ExpressionData expressionData = isExpressionValid && expressionDataSection != null ?
                expressionDataSection.hasExpression(currExpression) ?
                        expressionDataSection.getExpression(currExpression) : // Retrieve expression from puppet's animation manager
                null : null;
        ExpressionFaceData faceData = isExpressionValid && expressionData != null ?
                expressionData.areas.stream().anyMatch((x) -> x.getBone().equals(bone.name)) ? // Check if the expression contains this bone
                        expressionData.areas.stream().anyMatch((x) -> x.getCube() == cubeID) ?  // Check if the expression contains this cube's ID
                                expressionData.areas.stream().filter((x) -> x.getCube() == cubeID).findFirst().get() : // Get the cube by ID within the bone
                        null : null : null;

        if (faceData != null) {
            double[] textureSize = new double[]{getGeoModelProvider().getModel(puppet.sourceManager.model.getSafe()).properties.getTextureWidth(), getGeoModelProvider().getModel(puppet.sourceManager.model.getSafe()).properties.getTextureHeight()};

            for (GeoQuad quad : cube.quads) {
                if (quad == null)
                    continue;
                renderQuadWithExpression(matrix3f, matrix4f, textureSize, faceData, cube, quad, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            }
        } else {
            for (GeoQuad quad : cube.quads) {
                if (quad == null)
                    continue;
                renderQuad(matrix3f, matrix4f, cube, quad, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            }
        }
    }


    public void renderQuad(Matrix3f matrix3f, Matrix4f matrix4f, GeoCube cube, GeoQuad quad, VertexConsumer bufferIn, int packedLightIn,
                           int packedOverlayIn, float red, float green, float blue, float alpha) {
        Vector3f normal = quad.normal.copy();
        normal.transform(matrix3f);

        // Fix shading dark shading for flat cubes + compatibility with Optifine shaders
        if ((cube.size.y() == 0 || cube.size.z() == 0) && normal.x() < 0) normal.mul(-1, 1, 1);
        if ((cube.size.x() == 0 || cube.size.z() == 0) && normal.y() < 0) normal.mul(1, -1, 1);
        if ((cube.size.x() == 0 || cube.size.y() == 0) && normal.z() < 0) normal.mul(1, 1, -1);

        for (GeoVertex vertex : quad.vertices) {
            Vector4f vector4f = new Vector4f(vertex.position.x(), vertex.position.y(), vertex.position.z(), 1.0F);
            vector4f.transform(matrix4f);
            bufferIn.vertex(vector4f.x(), vector4f.y(), vector4f.z(), red, green, blue, alpha,
                    vertex.textureU, vertex.textureV, packedOverlayIn, packedLightIn, normal.x(), normal.y(),
                    normal.z());
        }
    }

    public void renderQuadWithExpression(Matrix3f matrix3f, Matrix4f matrix4f, double[] textureSize, ExpressionFaceData faceData, GeoCube cube, GeoQuad quad, VertexConsumer bufferIn, int packedLightIn,
                            int packedOverlayIn, float red, float green, float blue, float alpha) {

        Vector3f normal = quad.normal.copy();
        normal.transform(matrix3f);

        // Fix shading dark shading for flat cubes + compatibility with Optifine shaders
        if ((cube.size.y() == 0 || cube.size.z() == 0) && normal.x() < 0) normal.mul(-1, 1, 1);
        if ((cube.size.x() == 0 || cube.size.z() == 0) && normal.y() < 0) normal.mul(1, -1, 1);
        if ((cube.size.x() == 0 || cube.size.y() == 0) && normal.z() < 0) normal.mul(1, 1, -1);

        // Sorts verts into smallest to largest
        GeoVertex[] verts = quad.vertices.clone();
        int[] vertIDs = new int[verts.length];
        float[] minMaxUVs = new float[] { Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE }; // minU, minV, maxU, maxV
        for (int i = 0; i < verts.length; i++){
            if (minMaxUVs[0] > verts[i].textureU) {minMaxUVs[0] = verts[i].textureU; vertIDs[0] = i;}
            if (minMaxUVs[1] > verts[i].textureV) {minMaxUVs[1] = verts[i].textureV; vertIDs[1] = i;}
            if (minMaxUVs[2] < verts[i].textureU) {minMaxUVs[2] = verts[i].textureU; vertIDs[2] = i;}
            if (minMaxUVs[3] < verts[i].textureV) {minMaxUVs[3] = verts[i].textureV; vertIDs[3] = i;}
        }

        for (GeoVertex vertex : quad.vertices) {
            float u = faceData.getNewPosition()[0] / (float)textureSize[0];
            float v = faceData.getNewPosition()[1] / (float)textureSize[1];
            float w = minMaxUVs[2] == vertex.textureU ? verts[vertIDs[2]].textureU - verts[vertIDs[0]].textureU : 0;
            float h = minMaxUVs[3] == vertex.textureV ? verts[vertIDs[3]].textureV - verts[vertIDs[1]].textureV : 0;
            u += w;
            v += h;
            Vector4f vector4f = new Vector4f(vertex.position.x(), vertex.position.y(), vertex.position.z(), 1.0F);
            vector4f.transform(matrix4f);
            bufferIn.vertex(vector4f.x(), vector4f.y(), vector4f.z(), red, green, blue, alpha,
                    u, v, packedOverlayIn, packedLightIn, normal.x(), normal.y(),
                    normal.z());
        }
    }

    @Override
    public void render(PuppetEntity puppet, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLightIn) {
        if (puppet.isCompletelyDead() && !puppet.renderManager.canRenderHiddenDeathEffects()) {
            // Don't render
            return;
        }
        poseStack.pushPose();
        final float scale = puppet.getScale();
        poseStack.scale(scale, scale, scale);
        final int light = !puppet.renderManager.ignoreLighting.get() ? packedLightIn : 0xF000F0;
        super.render(puppet, entityYaw, partialTicks, poseStack, buffers, light);
        poseStack.popPose();
        if (entityRenderDispatcher.shouldRenderHitBoxes()) {
            poseStack.pushPose();
            final AABB aabb = puppet.getBoundingBoxForCulling().move(puppet.position().reverse());
            final VertexConsumer buffer = buffers.getBuffer(RenderType.lines());
            LevelRenderer.renderLineBox(poseStack, buffer, aabb, 0.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        }
    }

    @Override
    protected void applyRotations(PuppetEntity puppet, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.applyRotations(puppet, poseStack, ageInTicks, rotationYaw, partialTicks);
        if (puppet.easterEggs.isActive(ERROR)) {
            poseStack.scale(0.6F, 1.0F, 1.0F);
        }
    }

    @Override
    public RenderType getRenderType(PuppetEntity puppet, float partialTicks, PoseStack poseStack, @Nullable MultiBufferSource buffers, @Nullable VertexConsumer buffer, int packedLightIn, ResourceLocation textureLocation) {
        if (puppet.renderManager.canRenderHiddenDeathEffects()) {
            return RenderType.entityTranslucent(textureLocation);
        }
        return puppet.renderManager.getRenderType(textureLocation);
    }

    @Override
    public Color getRenderColor(PuppetEntity puppet, float partialTicks, PoseStack poseStack, @Nullable MultiBufferSource buffers, @Nullable VertexConsumer buffer, int packedLightIn) {
        if (puppet.easterEggs.isActive(ERROR)) {
            final float b = ((float) Math.sin(puppet.level.getGameTime() / 3.0D) + 1.0F) / 3.34F + 0.4F;
            return Color.ofHSB(0.0F, 1.0F, b);
        } else {
            final Color color = Color.WHITE; // puppet.renderManager.tintColor.get();
            if (puppet.renderManager.canRenderHiddenDeathEffects()) {
                return Color.ofRGBA(color.getRed(), color.getGreen() / 3, color.getBlue() / 3, color.getAlpha() / 4);
            }
            return color;
        }
    }

    @Override
    protected float getDeathMaxRotation(PuppetEntity puppet) {
        return (!puppet.animationManager.death.get().isEmpty() && puppet.animationManager.death.isValid()) || puppet.renderManager.canRenderHiddenDeathEffects() ? 0.0F : 90.0F;
    }

    @Override
    public boolean shouldShowName(PuppetEntity puppet) {
        final NameplateBehavior nameplateBehavior = puppet.renderManager.nameplateBehavior.get();
        return nameplateBehavior != NameplateBehavior.NEVER && (super.shouldShowName(puppet) || nameplateBehavior == NameplateBehavior.ALWAYS);
    }
}
