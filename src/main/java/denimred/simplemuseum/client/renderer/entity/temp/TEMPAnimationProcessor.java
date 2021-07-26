package denimred.simplemuseum.client.renderer.entity.temp;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimatableModel;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.keyframe.AnimationPoint;
import software.bernie.geckolib3.core.keyframe.BoneAnimationQueue;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.core.snapshot.BoneSnapshot;
import software.bernie.geckolib3.core.snapshot.DirtyTracker;
import software.bernie.geckolib3.core.util.MathUtil;
import software.bernie.shadowed.eliotlash.molang.MolangParser;

@Deprecated // TODO: Fix GeckoLib
public class TEMPAnimationProcessor extends AnimationProcessor<PuppetEntity> {
    public TEMPAnimationProcessor(IAnimatableModel model) {
        super(model);
    }

    public void tickAnimation(
            IAnimatable entity,
            Integer uniqueID,
            double seekTime,
            AnimationEvent event,
            MolangParser parser,
            boolean crashWhenCantFindBone) {
        AnimationData data = entity.getFactory().getOrCreateAnimationData(uniqueID);
        HashMap<String, DirtyTracker> trackers = this.createDirtyTrackers();
        this.updateBoneSnapshots(data.getBoneSnapshotCollection());
        HashMap<String, Pair<IBone, BoneSnapshot>> boneSnapshots = data.getBoneSnapshotCollection();

        IBone bone;
        BoneSnapshot snapshot;
        BoneSnapshot initialSnapshot;
        for (AnimationController<PuppetEntity> controller :
                data.getAnimationControllers().values()) {
            final HashMap<String, BoneAnimationQueue> queues = controller.getBoneAnimationQueues();
            if (this.reloadAnimations) {
                controller.markNeedsReload();
                queues.clear();
            }

            controller.isJustStarting = data.isFirstTick;
            event.setController(controller);
            controller.process(
                    seekTime,
                    event,
                    this.getModelRendererList(),
                    boneSnapshots,
                    parser,
                    crashWhenCantFindBone);
            for (BoneAnimationQueue queue : queues.values()) {
                bone = queue.bone;
                initialSnapshot = boneSnapshots.get(bone.getName()).getRight();
                snapshot = bone.getInitialSnapshot();
                AnimationPoint rXPoint = queue.rotationXQueue.poll();
                AnimationPoint rYPoint = queue.rotationYQueue.poll();
                AnimationPoint rZPoint = queue.rotationZQueue.poll();
                AnimationPoint pXPoint = queue.positionXQueue.poll();
                AnimationPoint pYPoint = queue.positionYQueue.poll();
                AnimationPoint pZPoint = queue.positionZQueue.poll();
                AnimationPoint sXPoint = queue.scaleXQueue.poll();
                AnimationPoint sYPoint = queue.scaleYQueue.poll();
                AnimationPoint sZPoint = queue.scaleZQueue.poll();
                DirtyTracker tracker = trackers.get(bone.getName());
                if (tracker != null) {
                    if (rXPoint != null && rYPoint != null && rZPoint != null) {
                        bone.setRotationX(
                                MathUtil.lerpValues(
                                                rXPoint,
                                                controller.easingType,
                                                controller.customEasingMethod)
                                        + snapshot.rotationValueX);
                        bone.setRotationY(
                                MathUtil.lerpValues(
                                                rYPoint,
                                                controller.easingType,
                                                controller.customEasingMethod)
                                        + snapshot.rotationValueY);
                        bone.setRotationZ(
                                MathUtil.lerpValues(
                                                rZPoint,
                                                controller.easingType,
                                                controller.customEasingMethod)
                                        + snapshot.rotationValueZ);
                        initialSnapshot.rotationValueX = bone.getRotationX();
                        initialSnapshot.rotationValueY = bone.getRotationY();
                        initialSnapshot.rotationValueZ = bone.getRotationZ();
                        initialSnapshot.isCurrentlyRunningRotationAnimation = true;
                        tracker.hasRotationChanged = true;
                    }

                    if (pXPoint != null && pYPoint != null && pZPoint != null) {
                        bone.setPositionX(
                                MathUtil.lerpValues(
                                        pXPoint,
                                        controller.easingType,
                                        controller.customEasingMethod));
                        bone.setPositionY(
                                MathUtil.lerpValues(
                                        pYPoint,
                                        controller.easingType,
                                        controller.customEasingMethod));
                        bone.setPositionZ(
                                MathUtil.lerpValues(
                                        pZPoint,
                                        controller.easingType,
                                        controller.customEasingMethod));
                        initialSnapshot.positionOffsetX = bone.getPositionX();
                        initialSnapshot.positionOffsetY = bone.getPositionY();
                        initialSnapshot.positionOffsetZ = bone.getPositionZ();
                        initialSnapshot.isCurrentlyRunningPositionAnimation = true;
                        tracker.hasPositionChanged = true;
                    }

                    if (sXPoint != null && sYPoint != null && sZPoint != null) {
                        bone.setScaleX(
                                MathUtil.lerpValues(
                                        sXPoint,
                                        controller.easingType,
                                        controller.customEasingMethod));
                        bone.setScaleY(
                                MathUtil.lerpValues(
                                        sYPoint,
                                        controller.easingType,
                                        controller.customEasingMethod));
                        bone.setScaleZ(
                                MathUtil.lerpValues(
                                        sZPoint,
                                        controller.easingType,
                                        controller.customEasingMethod));
                        initialSnapshot.scaleValueX = bone.getScaleX();
                        initialSnapshot.scaleValueY = bone.getScaleY();
                        initialSnapshot.scaleValueZ = bone.getScaleZ();
                        initialSnapshot.isCurrentlyRunningScaleAnimation = true;
                        tracker.hasScaleChanged = true;
                    }
                }
            }
        }

        this.reloadAnimations = false;
        double resetTickLength = data.getResetSpeed();

        for (Map.Entry<String, DirtyTracker> entry : trackers.entrySet()) {
            bone = entry.getValue().model;
            initialSnapshot = bone.getInitialSnapshot();
            snapshot = boneSnapshots.get(entry.getKey()).getRight();
            if (snapshot == null) {
                if (crashWhenCantFindBone) {
                    throw new RuntimeException(
                            "Could not find save snapshot for bone: "
                                    + entry.getValue().model.getName()
                                    + ". Please don't add bones that are used in an animation at runtime.");
                }
            } else {
                double percentageReset;
                if (!entry.getValue().hasRotationChanged) {
                    if (snapshot.isCurrentlyRunningRotationAnimation) {
                        snapshot.mostRecentResetRotationTick = (float) seekTime;
                        snapshot.isCurrentlyRunningRotationAnimation = false;
                    }

                    percentageReset =
                            Math.min(
                                    (seekTime - (double) snapshot.mostRecentResetRotationTick)
                                            / resetTickLength,
                                    1.0D);
                    bone.setRotationX(
                            MathUtil.lerpValues(
                                    percentageReset,
                                    snapshot.rotationValueX,
                                    initialSnapshot.rotationValueX));
                    bone.setRotationY(
                            MathUtil.lerpValues(
                                    percentageReset,
                                    snapshot.rotationValueY,
                                    initialSnapshot.rotationValueY));
                    bone.setRotationZ(
                            MathUtil.lerpValues(
                                    percentageReset,
                                    snapshot.rotationValueZ,
                                    initialSnapshot.rotationValueZ));
                    if (percentageReset >= 1.0D) {
                        snapshot.rotationValueX = bone.getRotationX();
                        snapshot.rotationValueY = bone.getRotationY();
                        snapshot.rotationValueZ = bone.getRotationZ();
                    }
                }

                if (!entry.getValue().hasPositionChanged) {
                    if (snapshot.isCurrentlyRunningPositionAnimation) {
                        snapshot.mostRecentResetPositionTick = (float) seekTime;
                        snapshot.isCurrentlyRunningPositionAnimation = false;
                    }

                    percentageReset =
                            Math.min(
                                    (seekTime - (double) snapshot.mostRecentResetPositionTick)
                                            / resetTickLength,
                                    1.0D);
                    bone.setPositionX(
                            MathUtil.lerpValues(
                                    percentageReset,
                                    snapshot.positionOffsetX,
                                    initialSnapshot.positionOffsetX));
                    bone.setPositionY(
                            MathUtil.lerpValues(
                                    percentageReset,
                                    snapshot.positionOffsetY,
                                    initialSnapshot.positionOffsetY));
                    bone.setPositionZ(
                            MathUtil.lerpValues(
                                    percentageReset,
                                    snapshot.positionOffsetZ,
                                    initialSnapshot.positionOffsetZ));
                    if (percentageReset >= 1.0D) {
                        snapshot.positionOffsetX = bone.getPositionX();
                        snapshot.positionOffsetY = bone.getPositionY();
                        snapshot.positionOffsetZ = bone.getPositionZ();
                    }
                }

                if (!entry.getValue().hasScaleChanged) {
                    if (snapshot.isCurrentlyRunningScaleAnimation) {
                        snapshot.mostRecentResetScaleTick = (float) seekTime;
                        snapshot.isCurrentlyRunningScaleAnimation = false;
                    }

                    percentageReset =
                            Math.min(
                                    (seekTime - (double) snapshot.mostRecentResetScaleTick)
                                            / resetTickLength,
                                    1.0D);
                    bone.setScaleX(
                            MathUtil.lerpValues(
                                    percentageReset,
                                    snapshot.scaleValueX,
                                    initialSnapshot.scaleValueX));
                    bone.setScaleY(
                            MathUtil.lerpValues(
                                    percentageReset,
                                    snapshot.scaleValueY,
                                    initialSnapshot.scaleValueY));
                    bone.setScaleZ(
                            MathUtil.lerpValues(
                                    percentageReset,
                                    snapshot.scaleValueZ,
                                    initialSnapshot.scaleValueZ));
                    if (percentageReset >= 1.0D) {
                        snapshot.scaleValueX = bone.getScaleX();
                        snapshot.scaleValueY = bone.getScaleY();
                        snapshot.scaleValueZ = bone.getScaleZ();
                    }
                }
            }
        }

        data.isFirstTick = false;
    }

    private void updateBoneSnapshots(
            HashMap<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection) {
        for (IBone bone : this.getModelRendererList()) {
            if (!boneSnapshotCollection.containsKey(bone.getName())) {
                boneSnapshotCollection.put(
                        bone.getName(), Pair.of(bone, new BoneSnapshot(bone.getInitialSnapshot())));
            }
        }
    }

    private HashMap<String, DirtyTracker> createDirtyTrackers() {
        HashMap<String, DirtyTracker> tracker = new HashMap<>();
        for (IBone bone : this.getModelRendererList()) {
            tracker.put(bone.getName(), new DirtyTracker(false, false, false, bone));
        }
        return tracker;
    }
}
