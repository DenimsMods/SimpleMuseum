package denimred.simplemuseum.client.renderer.entity.temp;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.Collections;

import javax.annotation.Nullable;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.exception.GeckoLibException;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

@Deprecated // TODO: Fix GeckoLib
public abstract class TEMPAnimatedGeoModel extends AnimatedGeoModel<PuppetEntity> {
    private final AnimationProcessor animationProcessor;
    private GeoModel currentModel;

    protected TEMPAnimatedGeoModel() {
        animationProcessor = new TEMPAnimationProcessor(this);
    }

    @Override
    public void setLivingAnimations(
            PuppetEntity puppet, Integer id, @Nullable AnimationEvent customPredicate) {
        final long tick = this.getCurrentTick(puppet);
        AnimationData data = puppet.getFactory().getOrCreateAnimationData(id);
        if (data.startTick == null) {
            data.startTick = (double) tick;
        }

        AnimationEvent<PuppetEntity> predicate;
        if (customPredicate == null) {
            predicate = new AnimationEvent<>(puppet, 0, 0, 1.0F, false, Collections.emptyList());
        } else {
            predicate = customPredicate;
        }

        final long time = tick - data.startTick.longValue();
        final float partialTick = predicate.getPartialTick();
        // This appears to cause mild stuttering and skipping, but... whatever
        final double seekTime = MathHelper.lerp(partialTick, time, time + 1);
        predicate.animationTick = data.tick = seekTime;

        animationProcessor.preAnimationSetup(predicate.getAnimatable(), seekTime);
        if (!animationProcessor.getModelRendererList().isEmpty()) {
            animationProcessor.tickAnimation(
                    puppet,
                    id,
                    seekTime,
                    predicate,
                    GeckoLibCache.getInstance().parser,
                    shouldCrashOnMissing);
        }
    }

    private long getCurrentTick(PuppetEntity puppet) {
        if (puppet.world != null) {
            return puppet.world.getGameTime();
        } else {
            return (long) this.getCurrentTick();
        }
    }

    @Override
    public AnimationProcessor getAnimationProcessor() {
        return animationProcessor;
    }

    @Override
    public void registerModelRenderer(IBone modelRenderer) {
        animationProcessor.registerModelRenderer(modelRenderer);
    }

    @Override
    public GeoModel getModel(ResourceLocation location) {
        GeoModel model = super.getModel(location);
        if (model == null) {
            throw new GeckoLibException(
                    location,
                    "Could not find model. If you are getting this with a built mod, please just restart your game.");
        }
        if (model != currentModel) {
            animationProcessor.clearModelRendererList();
            for (GeoBone bone : model.topLevelBones) {
                registerBone(bone);
            }
            currentModel = model;
        }
        return model;
    }
}
