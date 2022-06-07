package denimred.simplemuseum.client.renderer.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import denimred.simplemuseum.client.renderer.entity.PuppetModel;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.init.MuseumItems;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.util.RenderUtils;

public class PuppetHeldItemLayerRenderer extends GeoLayerRenderer<PuppetEntity> {
    private final HashMap<GeoBone, ItemStack> heldItems = new HashMap<>();
    private static final ItemStack testStack = new ItemStack(MuseumItems.CURATORS_CANE.get());

    public PuppetHeldItemLayerRenderer(IGeoRenderer<PuppetEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, PuppetEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        GeoModel model = getEntityModel().getModel(getEntityModel().getModelLocation(entity));
        for(GeoBone bone : model.topLevelBones) {
            renderBoneAndChildren(bone, poseStack, bufferSource, light, entity);
        }
    }

    private void renderBoneAndChildren(GeoBone bone, PoseStack poseStack, MultiBufferSource bufferSource, int light, PuppetEntity puppet) {
        ItemStack itemStack = testStack;
        poseStack.pushPose();

        RenderUtils.moveToPivot(bone, poseStack);
        RenderUtils.rotate(bone, poseStack);
        RenderUtils.scale(bone, poseStack);
        poseStack.translate(0, 0, -0.05);
        poseStack.scale(.7f,.7f,.7f);

        if(bone.childBones.isEmpty() && bone.childCubes.isEmpty())
            Minecraft.getInstance().getItemInHandRenderer().renderItem(puppet, itemStack, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, false, poseStack, bufferSource, light);

        poseStack.popPose();
        poseStack.pushPose();

        RenderUtils.translate(bone, poseStack);
        RenderUtils.moveToPivot(bone, poseStack);
        RenderUtils.rotate(bone, poseStack);
        RenderUtils.scale(bone, poseStack);
        RenderUtils.moveBackFromPivot(bone, poseStack);

        for(GeoBone child : bone.childBones)
            renderBoneAndChildren(child, poseStack, bufferSource, light, puppet);

        poseStack.popPose();
    }

}
