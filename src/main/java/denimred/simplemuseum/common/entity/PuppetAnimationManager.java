package denimred.simplemuseum.common.entity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;

import java.util.function.Predicate;

import denimred.simplemuseum.common.util.CheckedResource;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.resource.GeckoLibCache;

import static net.minecraftforge.common.util.Constants.NBT.TAG_STRING;

public class PuppetAnimationManager extends PuppetManager {
    // The root NBT key that this manager uses
    public static final String ANIMATION_MANAGER_NBT = "AnimationManager";
    // NBT keys for managed variables
    public static final String IDLE_NBT = "Idle";
    public static final String MOVING_NBT = "Moving";
    public static final String DEATH_NBT = "Death";
    // Data keys for managed variables
    public static final DataParameter<String> IDLE_KEY =
            MuseumPuppetEntity.createKeyContextual(DataSerializers.STRING);
    public static final DataParameter<String> MOVING_KEY =
            MuseumPuppetEntity.createKeyContextual(DataSerializers.STRING);
    public static final DataParameter<String> DEATH_KEY =
            MuseumPuppetEntity.createKeyContextual(DataSerializers.STRING);
    // Defaults for managed variables
    public static final String IDLE_DEFAULT = "";
    public static final String MOVING_DEFAULT = "";
    public static final String DEATH_DEFAULT = "";
    // Unmanaged variables
    final AnimationFactory factory = new AnimationFactory(puppet);
    final AnimationController<MuseumPuppetEntity> controller =
            new AnimationController<>(puppet, "controller", 3, this::getAnimState);
    // Managed variables
    public final CheckedResource<String> idle =
            new CheckedResource<>(
                    IDLE_DEFAULT,
                    (Predicate<String>) this::validateAnimation,
                    anim -> {
                        dataManager.set(IDLE_KEY, anim);
                        controller.markNeedsReload();
                    });
    public final CheckedResource<String> moving =
            new CheckedResource<>(
                    MOVING_DEFAULT,
                    (Predicate<String>) this::validateAnimation,
                    anim -> {
                        dataManager.set(MOVING_KEY, anim);
                        controller.markNeedsReload();
                    });
    public final CheckedResource<String> death =
            new CheckedResource<>(
                    DEATH_DEFAULT,
                    (Predicate<String>) this::validateAnimation,
                    anim -> {
                        dataManager.set(DEATH_KEY, anim);
                        controller.markNeedsReload();
                    });

    public PuppetAnimationManager(MuseumPuppetEntity puppet) {
        super(puppet, ANIMATION_MANAGER_NBT);
        controller.registerSoundListener(puppet.audioManager::playAnimSound);
    }

    private boolean validateAnimation(String anim) {
        return this.validateAnimation(anim, true);
    }

    private boolean validateAnimation(String anim, boolean allowEmpty) {
        return allowEmpty && anim.isEmpty()
                || puppet.sourceManager.animations.isValid()
                        && GeckoLibCache.getInstance()
                                        .getAnimations()
                                        .get(puppet.sourceManager.animations.getDirect())
                                        .getAnimation(anim)
                                != null;
    }

    private <T extends IAnimatable> PlayState getAnimState(AnimationEvent<T> event) {
        final AnimationState state = controller.getAnimationState();
        // Death animation, overrides all
        if (puppet.isDead() && !puppet.isCompletelyDead() && this.playAnimInternal(death, false)) {
            return PlayState.CONTINUE;
        }
        // Current animation is inaccurate when transitioning, so just continue until we're done
        if (state == AnimationState.Transitioning) {
            return PlayState.CONTINUE;
        }
        // Continue playing the current non-looping animation until it's done
        final Animation current = controller.getCurrentAnimation();
        if (current != null && !current.loop && state != AnimationState.Stopped) {
            return PlayState.CONTINUE;
        }
        // Moving animation, overrides idle
        if (event.isMoving() && this.playAnimInternal(moving, true)) {
            return PlayState.CONTINUE;
        }
        // Idle animation last, as a fallback
        if (this.playAnimInternal(idle, true)) {
            return PlayState.CONTINUE;
        }
        // If no valid animation exist, just stop and return to reference pose
        return PlayState.STOP;
    }

    private boolean playAnimInternal(CheckedResource<String> checked, boolean loop) {
        if (checked.isValid()) {
            final String anim = checked.getSafe();
            if (!anim.isEmpty()) {
                controller.setAnimation(new AnimationBuilder().addAnimation(anim, loop));
                return true;
            }
        }
        return false;
    }

    public void playAnimOnce(String anim) {
        if (this.validateAnimation(anim, false)) {
            controller.setAnimation(new AnimationBuilder().addAnimation(anim, false));
            controller.markNeedsReload();
        }
    }

    public double getDeathLength() {
        // TODO: Make this configurable
        return this.hasDeathAnim() ? 32 : 20;
    }

    public boolean hasDeathAnim() {
        return !puppet.animationManager.death.getDirect().isEmpty()
                && puppet.animationManager.death.isValid();
    }

    @Override
    public void registerDataKeys() {
        dataManager.register(IDLE_KEY, IDLE_DEFAULT);
        dataManager.register(MOVING_KEY, MOVING_DEFAULT);
        dataManager.register(DEATH_KEY, DEATH_DEFAULT);
    }

    @Override
    public void onDataChanged(DataParameter<?> key) {
        if (key.equals(IDLE_KEY)) idle.set(dataManager.get(IDLE_KEY));
        else if (key.equals(MOVING_KEY)) moving.set(dataManager.get(MOVING_KEY));
        else if (key.equals(DEATH_KEY)) death.set(dataManager.get(DEATH_KEY));
    }

    @Override
    public void readNBT(CompoundNBT tag) {
        if (tag.contains(IDLE_NBT, TAG_STRING)) idle.set(tag.getString(IDLE_NBT));
        if (tag.contains(MOVING_NBT, TAG_STRING)) moving.set(tag.getString(MOVING_NBT));
        if (tag.contains(DEATH_NBT, TAG_STRING)) death.set(tag.getString(DEATH_NBT));
    }

    @Override
    public void writeNBT(CompoundNBT tag) {
        final String idleAnim = idle.getDirect();
        if (!idleAnim.equals(IDLE_DEFAULT)) tag.putString(IDLE_NBT, idleAnim);
        final String movingAnim = moving.getDirect();
        if (!movingAnim.equals(MOVING_DEFAULT)) tag.putString(MOVING_NBT, movingAnim);
        final String deathAnim = death.getDirect();
        if (!deathAnim.equals(DEATH_DEFAULT)) tag.putString(DEATH_NBT, deathAnim);
    }

    @Override
    public void remapNBT(CompoundNBT root) {
        this.remap140(root, "SelectedAnimation", IDLE_NBT);
    }

    @Override
    public void clearCaches() {
        idle.clearCache();
        moving.clearCache();
    }
}
