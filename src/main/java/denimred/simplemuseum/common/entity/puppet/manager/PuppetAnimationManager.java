package denimred.simplemuseum.common.entity.puppet.manager;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.resources.data.ExpressionData;
import denimred.simplemuseum.client.resources.data.ExpressionDataSection;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.checked.CheckedProvider;
import denimred.simplemuseum.common.entity.puppet.manager.value.checked.CheckedValue;
import denimred.simplemuseum.common.entity.puppet.manager.value.primitive.BoolProvider;
import denimred.simplemuseum.common.entity.puppet.manager.value.primitive.BoolValue;
import denimred.simplemuseum.common.entity.puppet.manager.value.primitive.IntProvider;
import denimred.simplemuseum.common.entity.puppet.manager.value.primitive.IntValue;
import denimred.simplemuseum.common.i18n.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.resource.GeckoLibCache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    public static final CheckedProvider<String> EXPRESSION =
            new CheckedProvider<>(
                    key("Expression"),
                    "",
                    PuppetAnimationManager::validateExpression);
    public static final BoolProvider EXPRESSIONS_ENABLED = new BoolProvider(key("ExpressionsEnabled"), true);

    public final CheckedValue<String> idle = this.value(IDLE);
    public final CheckedValue<String> moving = this.value(MOVING);
    public final CheckedValue<String> idleSneak = this.value(IDLE_SNEAK);
    public final CheckedValue<String> movingSneak = this.value(MOVING_SNEAK);
    public final CheckedValue<String> sprinting = this.value(SPRINTING);
    public final CheckedValue<String> sitting = this.value(SITTING);
    public final CheckedValue<String> death = this.value(DEATH);
    public final IntValue deathLength = this.value(DEATH_LENGTH);

    public String animatedExpression = "";
    public final BoolValue expressionsEnabled = this.value(EXPRESSIONS_ENABLED);
    public final CheckedValue<String> expression = this.value(EXPRESSION);
    public static final Map<ResourceLocation, ExpressionDataSection> EXPRESSION_DATA_CACHE = new HashMap<>();

    // TODO: JAKE WIPE THIS WHEN WE RELOAD RESOURCES

    public final AnimationFactory factory = new AnimationFactory(puppet);
    public final AnimationController<PuppetEntity> controller =
            new AnimationController<>(puppet, "controller", 3, this::getAnimState);

    public PuppetAnimationManager(PuppetEntity puppet) {
        super(puppet, NBT_KEY, TRANSLATION_KEY);
        controller.registerSoundListener(puppet.audioManager::playAnimSound);
        controller.registerCustomInstructionListener(this::expressionListener);
    }

    private static PuppetKey key(String provider) {
        return new PuppetKey(NBT_KEY, provider);
    }

    // This is kind of stupid
    public static void reloadController(PuppetEntity puppet, Object dummyForMethodReference) {
        puppet.animationManager.controller.markNeedsReload();
    }

    private static boolean validateExpression(PuppetEntity puppet, String expression) {
        if (puppet.level.isClientSide || expression.isEmpty()) return true;
        if (!puppet.sourceManager.model.isValid()) return false;
        ExpressionDataSection data = getExpressionData(puppet.sourceManager.model.getSafe());
        if (data == ExpressionDataSection.EMPTY) return false;
        return data.hasExpression(expression);
    }

    public static ExpressionDataSection getExpressionData(ResourceLocation location) {
        if (EXPRESSION_DATA_CACHE.containsKey(location))
            return EXPRESSION_DATA_CACHE.get(location);

        ResourceManager resourceManager =  Minecraft.getInstance().getResourceManager();
        try (Resource resource = resourceManager.getResource(location)) {

            ExpressionDataSection expressionData = resource.getMetadata(ExpressionDataSection.SERIALIZER);
            if (expressionData == null)
                expressionData = ExpressionDataSection.EMPTY;

            if (!EXPRESSION_DATA_CACHE.containsKey(location))
                EXPRESSION_DATA_CACHE.put(location, expressionData);
            SimpleMuseum.LOGGER.debug("Stored [" + location + "] into the Expression Data Cache");
            return expressionData;
        } catch (RuntimeException e){
            SimpleMuseum.LOGGER.debug("Unable to parse metadata from " + location + " : " + e);
        } catch (IOException e){
            SimpleMuseum.LOGGER.debug("Using missing model, unable to load " + location + " : " + e);
        }
        if (!EXPRESSION_DATA_CACHE.containsKey(location))
            EXPRESSION_DATA_CACHE.put(location, ExpressionDataSection.EMPTY);
        return ExpressionDataSection.EMPTY;
    }

    private <T extends IAnimatable> void expressionListener(CustomInstructionKeyframeEvent<T> event) {
        if (expressionsEnabled.get()) {
            if (event.instructions.startsWith("expression."))
                animatedExpression = event.instructions.substring("expression.".length());
        }
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
            if (current != null && !current.loop && state != AnimationState.Stopped) {
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
                controller.setAnimation(new AnimationBuilder().addAnimation(anim, loop));
                return true;
            }
        }
        return false;
    }

    public void playAnimOnce(String anim) {
        if (validateAnimation(puppet, anim, false)) {
            controller.setAnimation(new AnimationBuilder().addAnimation(anim, false));
            controller.markNeedsReload();
        }
    }

    public AnimationController<PuppetEntity> getController() {
        return controller;
    }


}
