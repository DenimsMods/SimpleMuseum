package denimred.simplemuseum.client.renderer.entity.temp;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;

import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.core.snapshot.BoneSnapshot;
import software.bernie.shadowed.eliotlash.molang.MolangParser;

@Deprecated // TODO: Fix GeckoLib
public class TEMPAnimationController<T extends IAnimatable> extends AnimationController<T> {
    public TEMPAnimationController(
            T animatable,
            String name,
            float transitionLengthTicks,
            IAnimationPredicate<T> animationPredicate) {
        super(animatable, name, transitionLengthTicks, animationPredicate);
    }

    @Override
    public void process(
            double tick,
            AnimationEvent event,
            List<IBone> modelRendererList,
            HashMap<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection,
            MolangParser parser,
            boolean crashWhenCantFindBone) {
        try {
            super.process(
                    tick,
                    event,
                    modelRendererList,
                    boneSnapshotCollection,
                    parser,
                    crashWhenCantFindBone);
        } catch (NullPointerException e) {
            // ugh
        }
    }
}
