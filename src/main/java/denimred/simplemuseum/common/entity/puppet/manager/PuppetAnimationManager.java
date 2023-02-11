package denimred.simplemuseum.common.entity.puppet.manager;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.checked.CheckedProvider;
import denimred.simplemuseum.common.entity.puppet.manager.value.checked.CheckedValue;
import denimred.simplemuseum.common.entity.puppet.manager.value.primitive.IntProvider;
import denimred.simplemuseum.common.entity.puppet.manager.value.primitive.IntValue;
import denimred.simplemuseum.common.i18n.I18nUtil;
import org.openjdk.nashorn.api.tree.LoopTree;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.core.manager.InstancedAnimationFactory;
import software.bernie.geckolib3.resource.GeckoLibCache;

import static software.bernie.geckolib3.core.builder.ILoopType.EDefaultLoopTypes.LOOP;
import static software.bernie.geckolib3.core.builder.ILoopType.EDefaultLoopTypes.PLAY_ONCE;

public final class PuppetAnimationManager extends PuppetValueManager {
    public static final String NBT_KEY = "AnimationManager";
    public static final String TRANSLATION_KEY = I18nUtil.valueManager(NBT_KEY);

    public static final CheckedProvider<String> IDLE =
            new CheckedProvider<>(
                    key("Idle"),
                    "",
                    PuppetAnimationManager::reloadController,
                    PuppetAnimationManager::validateAnimation);
    public static final CheckedProvider<String> MOVING =
            new CheckedProvider<>(
                    key("Moving"),
                    "",
                    PuppetAnimationManager::reloadController,
                    PuppetAnimationManager::validateAnimation);
    public static final CheckedProvider<String> IDLE_SNEAK =
            new CheckedProvider<>(
                    key("IdleSneak"),
                    "",
                    PuppetAnimationManager::reloadController,
                    PuppetAnimationManager::validateAnimation);
    public static final CheckedProvider<String> MOVING_SNEAK =
            new CheckedProvider<>(
                    key("MovingSneak"),
                    "",
                    PuppetAnimationManager::reloadController,
                    PuppetAnimationManager::validateAnimation);
    public static final CheckedProvider<String> SPRINTING =
            new CheckedProvider<>(
                    key("Sprinting"),
                    "",
                    PuppetAnimationManager::reloadController,
                    PuppetAnimationManager::validateAnimation);
    public static final CheckedProvider<String> SITTING =
            new CheckedProvider<>(
                    key("Sitting"),
                    "",
                    PuppetAnimationManager::reloadController,
                    PuppetAnimationManager::validateAnimation);
    public static final CheckedProvider<String> DEATH =
            new CheckedProvider<>(
                    key("Death"),
                    "",
                    PuppetAnimationManager::reloadController,
                    PuppetAnimationManager::validateAnimation);
    public static final IntProvider DEATH_LENGTH = new IntProvider(key("DeathLength"), 28, 0, 1200);

    public final CheckedValue<String> idle = this.value(IDLE);
    public final CheckedValue<String> moving = this.value(MOVING);
    public final CheckedValue<String> idleSneak = this.value(IDLE_SNEAK);
    public final CheckedValue<String> movingSneak = this.value(MOVING_SNEAK);
    public final CheckedValue<String> sprinting = this.value(SPRINTING);
    public final CheckedValue<String> sitting = this.value(SITTING);
    public final CheckedValue<String> death = this.value(DEATH);
    public final IntValue deathLength = this.value(DEATH_LENGTH);

    public final AnimationFactory factory = new InstancedAnimationFactory(puppet);
    public final AnimationController<PuppetEntity> controller =
            new AnimationController<>(puppet, "controller", 3, this::getAnimState);

    public PuppetAnimationManager(PuppetEntity puppet) {
        super(puppet, NBT_KEY, TRANSLATION_KEY);
        controller.registerSoundListener(puppet.audioManager::playAnimSound);
    }

    private static PuppetKey key(String provider) {
        return new PuppetKey(NBT_KEY, provider);
    }

    // This is kind of stupid
    public static void reloadController(PuppetEntity puppet, Object dummyForMethodReference) {
        puppet.animationManager.controller.markNeedsReload();
    }

    private static boolean validateAnimation(PuppetEntity puppet, String anim) {
        return validateAnimation(puppet, anim, true);
    }

    private static boolean validateAnimation(PuppetEntity puppet, String anim, boolean allowEmpty) {
        return !puppet.level.isClientSide
                || allowEmpty && anim.isEmpty()
                || puppet.sourceManager.animations.isValid()
                        && GeckoLibCache.getInstance()
                                        .getAnimations()
                                        .get(puppet.sourceManager.animations.get())
                                        .getAnimation(anim)
                                != null;
    }

    private <T extends IAnimatable> PlayState getAnimState(AnimationEvent<T> event) {
        if (puppet.sourceManager.animations.isValid()) {
            final AnimationState state = controller.getAnimationState();
            // Death animation, overrides all
            if (puppet.isDead()
                    && !puppet.isCompletelyDead()
                    && this.playAnimInternal(death, false)) {
                return PlayState.CONTINUE;
            }
            // Current animation is inaccurate when transitioning, so just continue until we're done
            if (state == AnimationState.Transitioning) {
                return PlayState.CONTINUE;
            }
            // Continue playing the current non-looping animation until it's done
            final Animation current = controller.getCurrentAnimation();
            if (current != null && !current.loop.isRepeatingAfterEnd() && state != AnimationState.Stopped) {
                return PlayState.CONTINUE;
            }
            // Sitting animation, override moving
            if (puppet.isSitting() && this.playAnimInternal(sitting, true)) {
                return PlayState.CONTINUE;
            }
            // Moving animation, overrides idle
            if (event.isMoving()) {
                if (puppet.isShiftKeyDown() && this.playAnimInternal(movingSneak, true)) {
                    return PlayState.CONTINUE;
                } else if (puppet.isSprinting() && this.playAnimInternal(sprinting, true)) {
                    return PlayState.CONTINUE;
                } else if (this.playAnimInternal(moving, true)) {
                    return PlayState.CONTINUE;
                }
            }
            // Idle animation last, as a fallback
            if (puppet.isShiftKeyDown() && this.playAnimInternal(idleSneak, true)) {
                return PlayState.CONTINUE;
            } else if (this.playAnimInternal(idle, true)) {
                return PlayState.CONTINUE;
            }
        }
        // If no valid animation exist, just stop and return to reference pose
        return PlayState.STOP;
    }

    private boolean playAnimInternal(CheckedValue<String> checked, boolean loop) {
        if (checked.isValid()) {
            final String anim = checked.getSafe();
            if (!anim.isEmpty()) {
                // TODO: Loop changes may cause issues
                controller.setAnimation(new AnimationBuilder().addAnimation(anim, loop ? LOOP : PLAY_ONCE));
                return true;
            }
        }
        return false;
    }

    public void playAnimOnce(String anim) {
        if (validateAnimation(puppet, anim, false)) {
            controller.setAnimation(new AnimationBuilder().addAnimation(anim, PLAY_ONCE));
            controller.markNeedsReload();
        }
    }

    public AnimationController<PuppetEntity> getController() {
        return controller;
    }


}
