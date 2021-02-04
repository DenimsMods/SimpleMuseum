package denimred.simplemuseum.common.entity;

import net.minecraft.block.material.PushReaction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Collections;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.init.MuseumDataSerializers;
import denimred.simplemuseum.common.init.MuseumEntities;
import denimred.simplemuseum.common.init.MuseumItems;
import denimred.simplemuseum.common.util.CheckedResource;
import denimred.simplemuseum.common.util.MathUtil;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.resource.GeckoLibCache;

import static net.minecraftforge.common.util.Constants.NBT.TAG_STRING;

// TODO: The specifics of this entity were just kind of thrown together; needs to be reviewed
public class MuseumDummyEntity extends LivingEntity implements IAnimatable {
    public static final ResourceLocation DEFAULT_MODEL_LOCATION =
            new ResourceLocation(SimpleMuseum.MOD_ID, "geo/entity/museum_dummy.geo.json");
    public static final ResourceLocation DEFAULT_TEXTURE_LOCATION =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/entity/museum_dummy.png");
    public static final ResourceLocation DEFAULT_ANIMATIONS_LOCATION =
            new ResourceLocation(SimpleMuseum.MOD_ID, "animations/entity/museum_dummy.json");
    public static final String DEFAULT_SELECTED_ANIMATION = "";
    public static final DataParameter<ResourceLocation> MODEL_LOCATION =
            EntityDataManager.createKey(
                    MuseumDummyEntity.class, MuseumDataSerializers.getResourceLocationSerializer());
    public static final DataParameter<ResourceLocation> TEXTURE_LOCATION =
            EntityDataManager.createKey(
                    MuseumDummyEntity.class, MuseumDataSerializers.getResourceLocationSerializer());
    public static final DataParameter<ResourceLocation> ANIMATIONS_LOCATION =
            EntityDataManager.createKey(
                    MuseumDummyEntity.class, MuseumDataSerializers.getResourceLocationSerializer());
    public static final DataParameter<String> SELECTED_ANIMATION =
            EntityDataManager.createKey(MuseumDummyEntity.class, DataSerializers.STRING);
    public static final String MODEL_NBT = "Model";
    public static final String TEXTURE_NBT = "Texture";
    public static final String ANIMATIONS_NBT = "Animations";
    public static final String SELECTED_ANIMATION_NBT = "SelectedAnimation";
    public static final String INVISIBLE_NBT = "Invisible";
    private final AnimationFactory factory = new AnimationFactory(this);
    private final CheckedResource<ResourceLocation> model =
            new CheckedResource<>(
                    DEFAULT_MODEL_LOCATION,
                    loc -> GeckoLibCache.getInstance().getGeoModels().get(loc) != null);
    private final CheckedResource<ResourceLocation> texture =
            new CheckedResource<>(
                    DEFAULT_TEXTURE_LOCATION,
                    loc -> Minecraft.getInstance().getResourceManager().getResource(loc));
    private final CheckedResource<ResourceLocation> animations =
            new CheckedResource<>(
                    DEFAULT_ANIMATIONS_LOCATION,
                    loc -> GeckoLibCache.getInstance().getAnimations().get(loc) != null);
    private final CheckedResource<String> selectedAnimation =
            new CheckedResource<>(
                    DEFAULT_SELECTED_ANIMATION,
                    anim -> {
                        if (anim.isEmpty()) {
                            return true;
                        } else {
                            final ResourceLocation animsLoc = animations.getDirect();
                            return animations.validate(animsLoc)
                                    && GeckoLibCache.getInstance()
                                                    .getAnimations()
                                                    .get(animsLoc)
                                                    .getAnimation(anim)
                                            != null;
                        }
                    });

    public MuseumDummyEntity(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Nullable
    public static MuseumDummyEntity spawn(
            ServerWorld world, Vector3d pos, @Nullable Entity entity) {
        return spawn(world, pos, entity != null ? entity.getPositionVec() : null);
    }

    @Nullable
    public static MuseumDummyEntity spawn(
            ServerWorld world, Vector3d pos, @Nullable Vector3d facing) {
        final MuseumDummyEntity dummy = MuseumEntities.MUSEUM_DUMMY.get().create(world);
        if (dummy != null) {
            final float yaw = facing != null ? MathUtil.angleBetween(pos, facing) : 0.0F;
            dummy.setLocationAndAngles(pos.x, pos.y, pos.z, yaw, 0.0F);
            world.func_242417_l(dummy);
        }
        return dummy;
    }

    @Override
    public void recalculateSize() {
        double x = this.getPosX();
        double y = this.getPosY();
        double z = this.getPosZ();
        super.recalculateSize();
        this.setPosition(x, y, z);
    }

    @Override
    protected void registerData() {
        super.registerData();
        dataManager.register(MODEL_LOCATION, DEFAULT_MODEL_LOCATION);
        dataManager.register(TEXTURE_LOCATION, DEFAULT_TEXTURE_LOCATION);
        dataManager.register(ANIMATIONS_LOCATION, DEFAULT_ANIMATIONS_LOCATION);
        dataManager.register(SELECTED_ANIMATION, DEFAULT_SELECTED_ANIMATION);
    }

    @Override
    public void writeAdditional(CompoundNBT tag) {
        super.writeAdditional(tag);
        tag.putBoolean(INVISIBLE_NBT, this.isInvisible());
        final CompoundNBT modTag = tag.getCompound(SimpleMuseum.MOD_ID);
        final ResourceLocation modelLocation = model.getDirect();
        if (!modelLocation.equals(DEFAULT_MODEL_LOCATION)) {
            modTag.putString(MODEL_NBT, modelLocation.toString());
        }
        final ResourceLocation textureLocation = texture.getDirect();
        if (!textureLocation.equals(DEFAULT_TEXTURE_LOCATION)) {
            modTag.putString(TEXTURE_NBT, textureLocation.toString());
        }
        final ResourceLocation animationsLocation = animations.getDirect();
        if (!animationsLocation.equals(DEFAULT_ANIMATIONS_LOCATION)) {
            modTag.putString(ANIMATIONS_NBT, animationsLocation.toString());
        }
        final String selAnim = selectedAnimation.getDirect();
        if (!selAnim.equals(DEFAULT_SELECTED_ANIMATION)) {
            modTag.putString(SELECTED_ANIMATION_NBT, selAnim);
        }
        if (!modTag.isEmpty()) {
            tag.put(SimpleMuseum.MOD_ID, modTag);
        }
    }

    @Override
    public void readAdditional(CompoundNBT tag) {
        super.readAdditional(tag);
        this.setInvisible(tag.getBoolean(INVISIBLE_NBT));
        final CompoundNBT modTag = tag.getCompound(SimpleMuseum.MOD_ID);
        try {
            if (modTag.contains(MODEL_NBT, TAG_STRING)) {
                this.setModelLocation(new ResourceLocation(modTag.getString(MODEL_NBT)));
            }
            if (modTag.contains(TEXTURE_NBT, TAG_STRING)) {
                this.setTextureLocation(new ResourceLocation(modTag.getString(TEXTURE_NBT)));
            }
            if (modTag.contains(ANIMATIONS_NBT, TAG_STRING)) {
                this.setAnimationsLocation(new ResourceLocation(modTag.getString(ANIMATIONS_NBT)));
            }
        } catch (ResourceLocationException e) {
            SimpleMuseum.LOGGER.error("Failed to load museum dummy file locations", e);
        }
        if (modTag.contains(SELECTED_ANIMATION_NBT, TAG_STRING)) {
            this.setSelectedAnimation(modTag.getString(SELECTED_ANIMATION_NBT));
        }
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(MuseumItems.CURATORS_CANE.get());
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {}

    @Override
    protected void collideWithNearbyEntities() {}

    @Override
    public ActionResultType applyPlayerInteraction(PlayerEntity player, Vector3d vec, Hand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (itemstack.getItem() != Items.NAME_TAG && player.isSpectator()) {
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return !this.isInvulnerableTo(source) && !this.world.isRemote && !this.getShouldBeDead();
    }

    @Override
    protected float updateDistance(float p_110146_1_, float p_110146_2_) {
        prevRenderYawOffset = prevRotationYaw;
        renderYawOffset = rotationYaw;
        return 0.0F;
    }

    @Override
    public void travel(Vector3d travelVector) {}

    @Override
    public void setRenderYawOffset(float offset) {
        prevRenderYawOffset = prevRotationYaw = offset;
        prevRotationYawHead = rotationYawHead = offset;
    }

    @Override
    public void setRotationYawHead(float rotation) {
        prevRenderYawOffset = prevRotationYaw = rotation;
        prevRotationYawHead = rotationYawHead = rotation;
    }

    @Override
    public boolean isChild() {
        return false;
    }

    @Override
    public void onKillCommand() {
        this.remove();
    }

    @Override
    public boolean isImmuneToExplosions() {
        return true;
    }

    @Override
    public PushReaction getPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean hitByEntity(Entity entityIn) {
        return !(entityIn instanceof PlayerEntity
                && world.isBlockModifiable((PlayerEntity) entityIn, this.getPosition()));
    }

    @Override
    protected SoundEvent getFallSound(int heightIn) {
        return SoundEvents.ENTITY_ARMOR_STAND_FALL;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_ARMOR_STAND_HIT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ARMOR_STAND_BREAK;
    }

    @Override
    public void func_241841_a(ServerWorld p_241841_1_, LightningBoltEntity p_241841_2_) {}

    @Override
    public boolean canBeHitWithPotion() {
        return false;
    }

    @Override
    public boolean attackable() {
        return false;
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getItemStackFromSlot(EquipmentSlotType slotIn) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack) {}

    @Override
    public HandSide getPrimaryHand() {
        return HandSide.RIGHT;
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (key.equals(MODEL_LOCATION)) {
            model.set(dataManager.get(MODEL_LOCATION));
        } else if (key.equals(TEXTURE_LOCATION)) {
            texture.set(dataManager.get(TEXTURE_LOCATION));
        } else if (key.equals(ANIMATIONS_LOCATION)) {
            animations.set(dataManager.get(ANIMATIONS_LOCATION));
        } else if (key.equals(SELECTED_ANIMATION)) {
            selectedAnimation.set(dataManager.get(SELECTED_ANIMATION));
        }
        super.notifyDataManagerChange(key);
    }

    public CheckedResource<ResourceLocation> getModelLocation() {
        return model;
    }

    public void setModelLocation(ResourceLocation modelLocation) {
        model.set(modelLocation);
        dataManager.set(MODEL_LOCATION, model.getDirect());
    }

    public CheckedResource<ResourceLocation> getTextureLocation() {
        return texture;
    }

    public void setTextureLocation(ResourceLocation textureLocation) {
        texture.set(textureLocation);
        dataManager.set(TEXTURE_LOCATION, texture.getDirect());
    }

    public CheckedResource<ResourceLocation> getAnimationsLocation() {
        return animations;
    }

    public void setAnimationsLocation(ResourceLocation animationsLocation) {
        animations.set(animationsLocation);
        dataManager.set(ANIMATIONS_LOCATION, animations.getDirect());
    }

    public CheckedResource<String> getSelectedAnimation() {
        return selectedAnimation;
    }

    public void setSelectedAnimation(String anim) {
        selectedAnimation.set(anim);
        dataManager.set(SELECTED_ANIMATION, selectedAnimation.getDirect());
    }

    public boolean doEasterEgg() {
        return this.getDisplayName().getUnformattedComponentText().equals(":)");
    }

    private <P extends IAnimatable> PlayState animationPredicate(AnimationEvent<P> event) {
        final String selAnim =
                ((MuseumDummyEntity) event.getAnimatable()).getSelectedAnimation().getSafe();
        if (!selAnim.isEmpty()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation(selAnim, true));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(
                new AnimationController<>(this, "controller", 0, this::animationPredicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return this.getBoundingBox().grow(10);
    }

    @Override
    public boolean isGlowing() {
        return super.isGlowing() || world.isRemote && ClientUtil.shouldDummyGlow(this);
    }

    public void clearAllCached() {
        model.clearCached();
        texture.clearCached();
        animations.clearCached();
        selectedAnimation.clearCached();
    }
}
