package denimred.simplemuseum.common.entity;

import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.HandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.init.MuseumEntities;
import denimred.simplemuseum.common.init.MuseumItems;
import denimred.simplemuseum.common.init.MuseumKeybinds;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.messages.s2c.ResurrectPuppetSync;
import denimred.simplemuseum.common.util.MathUtil;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class MuseumPuppetEntity extends LivingEntity implements IAnimatable {
    public final PuppetSourceManager sourceManager;
    public final PuppetBehaviorManager behaviorManager;
    public final PuppetAudioManager audioManager;
    public final PuppetAnimationManager animationManager;
    public final PuppetRenderManager renderManager;
    private final List<PuppetManager> managers;
    public int livingSoundTime;

    public MuseumPuppetEntity(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
        managers =
                Arrays.asList(
                        sourceManager = new PuppetSourceManager(this),
                        behaviorManager = new PuppetBehaviorManager(this),
                        audioManager = new PuppetAudioManager(this),
                        animationManager = new PuppetAnimationManager(this),
                        renderManager = new PuppetRenderManager(this));
    }

    // Forge gets angry about calling createKey from outside the defined class,
    // so we do this to contextualize the call and prove that we own the class.
    protected static <T> DataParameter<T> createKeyContextual(IDataSerializer<T> serializer) {
        return EntityDataManager.createKey(MuseumPuppetEntity.class, serializer);
    }

    @Nullable
    public static MuseumPuppetEntity spawn(
            ServerWorld world, Vector3d pos, @Nullable Entity entity) {
        return spawn(world, pos, entity != null ? entity.getPositionVec() : null);
    }

    @Nullable
    public static MuseumPuppetEntity spawn(
            ServerWorld world, Vector3d pos, @Nullable Vector3d facing) {
        final MuseumPuppetEntity puppet = MuseumEntities.MUSEUM_PUPPET.get().create(world);
        if (puppet != null) {
            final float yaw = facing != null ? MathUtil.yawBetween(pos, facing) : 0.0F;
            puppet.setLocationAndAngles(pos.x, pos.y, pos.z, yaw, 0.0F);
            world.func_242417_l(puppet);
        }
        return puppet;
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
    public void baseTick() {
        super.baseTick();
        if (this.isAlive() && rand.nextInt(1000) < livingSoundTime++) {
            livingSoundTime -= 80; // TODO: Make this configurable
            audioManager.playAmbientSound();
        }
    }

    @Override
    public SoundCategory getSoundCategory() {
        return audioManager.getCategory();
    }

    @Override
    public void writeAdditional(CompoundNBT tag) {
        super.writeAdditional(tag);
        final CompoundNBT modTag = tag.getCompound(SimpleMuseum.MOD_ID);
        this.writeModTag(modTag);
        if (!modTag.isEmpty()) {
            tag.put(SimpleMuseum.MOD_ID, modTag);
        }
    }

    public void writeModTag(CompoundNBT modTag) {
        managers.forEach(manager -> manager.write(modTag));
    }

    @Override
    public void readAdditional(CompoundNBT tag) {
        super.readAdditional(tag);
        this.readModTag(tag.getCompound(SimpleMuseum.MOD_ID));
    }

    public void readModTag(CompoundNBT modTag) {
        managers.forEach(manager -> manager.read(modTag));
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
    public boolean attackEntityFrom(DamageSource source, float amount) {
        // TODO: Handle damage instead of being invincible
        return !this.isInvulnerableTo(source) && !this.world.isRemote && !this.isDead();
    }

    @Override
    protected float updateDistance(float p_110146_1_, float p_110146_2_) {
        prevRenderYawOffset = prevRotationYaw;
        renderYawOffset = rotationYaw;
        return 0.0F;
    }

    @Override
    public void travel(Vector3d travelVector) {
        // TODO: Make the boolean here configurable (true to include Y value in limb swing calc)
        this.func_233629_a_(this, false);
    }

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
    public void onKillCommand() {
        // TODO: Handle damage instead of being invincible
        this.remove();
    }

    @Override
    protected void onDeathUpdate() {
        ++deathTime;
        if (deathTime == animationManager.getDeathLength()) {
            this.setInvisible(true);
            for (int i = 0; i < 20; ++i) {
                world.addParticle(
                        ParticleTypes.POOF,
                        this.getPosXRandom(1.0D),
                        this.getPosYRandom(),
                        this.getPosZRandom(1.0D),
                        rand.nextGaussian() * 0.02D,
                        rand.nextGaussian() * 0.02D,
                        rand.nextGaussian() * 0.02D);
            }
        }
    }

    public void resurrect() {
        this.setInvisible(this.isPotionActive(Effects.INVISIBILITY));
        this.setHealth(this.getMaxHealth());
        dead = false;
        deathTime = 0;
        if (!world.isRemote) {
            MuseumNetworking.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY.with(() -> this),
                    new ResurrectPuppetSync(this.getEntityId()));
        }
    }

    public boolean isDead() {
        return dead || deathTime > 0 || this.getShouldBeDead();
    }

    @Override
    public void heal(float healAmount) {
        if (!this.isDead()) {
            super.heal(healAmount);
        }
    }

    @Override
    public boolean isInvisibleToPlayer(PlayerEntity player) {
        if (this.isDead()) {
            return this.isInvisible() && !ClientUtil.isHoldingCane();
        } else {
            return super.isInvisibleToPlayer(player);
        }
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
        // TODO: Make this configurable
        return SoundEvents.ENTITY_ARMOR_STAND_FALL;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        // TODO: Make this configurable
        return null;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        // TODO: Make this configurable
        return null;
    }

    @Override
    public void causeLightningStrike(ServerWorld world, LightningBoltEntity lightning) {}

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
        // Sanity check is needed since superclass calls this method before the managers exist
        if (managers != null) managers.forEach(manager -> manager.onDataChanged(key));
        super.notifyDataManagerChange(key);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return renderManager.getRenderBounds();
    }

    @Override
    public boolean isGlowing() {
        return super.isGlowing() || world.isRemote && ClientUtil.shouldPuppetGlow(this);
    }

    @Override
    public int getTeamColor() {
        return renderManager.canRenderHiddenDeathEffects()
                ? 0xFF0000
                : MuseumKeybinds.GLOBAL_HIGHLIGHTS.isKeyDown()
                                && ClientUtil.getSelectedPuppet() != this
                        ? super.getTeamColor()
                        : 0x00FFFF;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(animationManager.controller);
    }

    @Override
    public AnimationFactory getFactory() {
        return animationManager.factory;
    }

    public void clearCaches() {
        managers.forEach(PuppetManager::clearCaches);
    }
}
