package denimred.simplemuseum.common.entity.puppet;

import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.HandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetAnimationManager;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetAudioManager;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetBehaviorManager;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetRenderManager;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetSourceManager;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;
import denimred.simplemuseum.common.init.MuseumEntities;
import denimred.simplemuseum.common.init.MuseumItems;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.item.CuratorsCaneItem;
import denimred.simplemuseum.common.network.messages.s2c.ResurrectPuppetSync;
import denimred.simplemuseum.common.util.GlowColor;
import denimred.simplemuseum.common.util.IValueSerializer;
import denimred.simplemuseum.common.util.MathUtil;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public final class PuppetEntity extends LivingEntity implements IAnimatable {
    public static final DataParameter<OptionalInt> POSSESSOR_ID =
            EntityDataManager.createKey(PuppetEntity.class, DataSerializers.OPTIONAL_VARINT);
    // Manager initialization order is different from normal due to NPEs when managers reference
    // each other during initialization
    public final PuppetSourceManager sourceManager = new PuppetSourceManager(this);
    public final PuppetRenderManager renderManager = new PuppetRenderManager(this);
    public final PuppetAudioManager audioManager = new PuppetAudioManager(this);
    public final PuppetAnimationManager animationManager = new PuppetAnimationManager(this);
    public final PuppetBehaviorManager behaviorManager = new PuppetBehaviorManager(this);
    public final PuppetEasterEggTracker easterEggs = new PuppetEasterEggTracker(this);
    protected final Map<String, PuppetValueManager> managers =
            new Object2ReferenceLinkedOpenHashMap<>();
    protected int livingSoundTime;
    @Nullable protected Entity possessor;

    public PuppetEntity(EntityType<? extends PuppetEntity> type, World world) {
        super(type, world);
        // Order is important since the config tab button icons are in a fixed order
        managers.put(sourceManager.nbtKey, sourceManager);
        managers.put(animationManager.nbtKey, animationManager);
        managers.put(renderManager.nbtKey, renderManager);
        managers.put(audioManager.nbtKey, audioManager);
        managers.put(behaviorManager.nbtKey, behaviorManager);
    }

    // Forge gets angry about calling createKey from outside the defined class,
    // so we do this to contextualize the call and prove that we own the class.
    // This is technically unsafe since anyone can call this, but whatever.
    public static <T> DataParameter<T> createKeyContextual(IValueSerializer<T> serializer) {
        final IDataSerializer<T> dataSerializer =
                serializer instanceof IValueSerializer.Wrapped
                        ? ((IValueSerializer.Wrapped<T>) serializer).parent
                        : serializer;
        return EntityDataManager.createKey(PuppetEntity.class, dataSerializer);
    }

    @Nullable
    public static PuppetEntity spawn(ServerWorld world, Vector3d pos, @Nullable Entity entity) {
        return spawn(world, pos, entity != null ? entity.getPositionVec() : null);
    }

    @Nullable
    public static PuppetEntity spawn(ServerWorld world, Vector3d pos, @Nullable Vector3d facing) {
        final PuppetEntity puppet = MuseumEntities.MUSEUM_PUPPET.get().create(world);
        if (puppet != null) {
            final float yaw = facing != null ? MathUtil.yawBetween(pos, facing) : 0.0F;
            puppet.setLocationAndAngles(pos.x, pos.y, pos.z, yaw, 0.0F);
            puppet.rotationYawHead = yaw;
            world.func_242417_l(puppet);
        }
        return puppet;
    }

    public static PuppetEntity makePreviewCopy(PuppetEntity original) {
        final PuppetEntity copy =
                Objects.requireNonNull(MuseumEntities.MUSEUM_PUPPET.get().create(original.world));
        copy.setEntityId(original.getEntityId());
        copy.setCustomName(original.getCustomName());
        copy.rotationYawHead = 0.0F;
        copy.rotationYaw = 0.0F;
        copy.rotationPitch = 0.0F;
        for (PuppetValueManager cm : copy.getManagers()) {
            final Optional<PuppetValueManager> om = original.getManager(cm.nbtKey);
            for (PuppetValue<?, ?> cv : cm.getValues()) {
                om.flatMap(m -> m.getValue(cv.provider))
                        .map(PuppetValue::get)
                        .ifPresent(cv::trySet);
            }
        }
        copy.animationManager.controller.markNeedsReload();
        return copy;
    }

    @Override
    protected void registerData() {
        super.registerData();
        dataManager.register(POSSESSOR_ID, OptionalInt.empty());
    }

    public Optional<PuppetValueManager> getManager(String managerKey) {
        return Optional.ofNullable(managers.get(managerKey));
    }

    public List<PuppetValueManager> getManagers() {
        return new LinkedList<>(managers.values());
    }

    public Optional<PuppetValue<?, ?>> getValue(PuppetKey key) {
        return this.getManager(key.manager).flatMap(m -> m.getValue(key.provider));
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
    public EntitySize getSize(Pose poseIn) {
        // TODO: Change for different poses?
        // Note: Not adjusting based on render scale for performance reasons
        return behaviorManager.physicalSize.get();
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (this.isAlive() && rand.nextInt(1000) < livingSoundTime++) {
            livingSoundTime -= 80; // TODO: Make this configurable
            audioManager.playAmbientSound();
        }
        if (this.world.isRemote) easterEggs.tick();
    }

    @Override
    public SoundCategory getSoundCategory() {
        return audioManager.category.get();
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
        PuppetDataHistorian.writeVersion(modTag);
        for (PuppetValueManager manager : managers.values()) {
            manager.write(modTag);
        }
    }

    @Override
    public void readAdditional(CompoundNBT tag) {
        super.readAdditional(tag);
        this.readModTag(tag.getCompound(SimpleMuseum.MOD_ID));
    }

    public void readModTag(CompoundNBT modTag) {
        PuppetDataHistorian.checkAndUpdate(modTag);
        for (PuppetValueManager manager : managers.values()) {
            manager.read(modTag);
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
    public boolean isInvulnerableTo(DamageSource source) {
        return (!this.isDamageable()) && source != DamageSource.OUT_OF_WORLD;
    }

    @Override
    public boolean canBeCollidedWith() {
        return world.isRemote && ClientUtil.isClientPossessing(this)
                ? ClientUtil.isHoldingCane()
                : super.canBeCollidedWith();
    }

    @Override
    protected float updateDistance(float yawOffset, float what) {
        if (possessor == null) {
            prevRenderYawOffset = prevRotationYaw;
            renderYawOffset = rotationYaw;
            return 0.0F;
        } else {
            this.renderYawOffset += MathHelper.wrapDegrees(yawOffset - this.renderYawOffset) * 0.3F;
            float yaw = MathHelper.wrapDegrees(this.rotationYaw - this.renderYawOffset);
            boolean flag = yaw < -90.0F || yaw >= 90.0F;
            if (yaw < -75.0F) {
                yaw = -75.0F;
            }

            if (yaw >= 75.0F) {
                yaw = 75.0F;
            }

            this.renderYawOffset = this.rotationYaw - yaw;
            if (yaw * yaw > 2500.0F) {
                this.renderYawOffset += yaw * 0.2F;
            }

            if (flag) {
                what *= -1.0F;
            }
            return what;
        }
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
        if (!this.isDamageable()) {
            this.remove();
        } else {
            super.onKillCommand();
        }
    }

    @Override
    public void onDeath(DamageSource cause) {
        if (this.isCompletelyDead() && cause == DamageSource.OUT_OF_WORLD) {
            this.remove();
        } else {
            super.onDeath(cause);
        }
    }

    @Override
    protected void onDeathUpdate() {
        ++deathTime;
        if (deathTime == animationManager.deathLength.get()) {
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
        this.setHealth(this.getMaxHealth());
        dead = false;
        deathTime = 0;
        if (!world.isRemote) {
            MuseumNetworking.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY.with(() -> this),
                    new ResurrectPuppetSync(this.getEntityId()));
        }
    }

    public boolean exists() {
        //noinspection deprecation
        return !removed;
    }

    public boolean isDead() {
        return dead || deathTime > 0 || this.getShouldBeDead();
    }

    public boolean isCompletelyDead() {
        return deathTime >= animationManager.deathLength.get();
    }

    public boolean isDamageable() {
        return false; // !invulnerable && !this.isDead(); TODO: Get this figured out
    }

    @Override
    public void heal(float healAmount) {
        if (!this.isDead()) {
            super.heal(healAmount);
        }
    }

    @Override
    public boolean isInvisibleToPlayer(PlayerEntity player) {
        if (super.isInvisibleToPlayer(player)) {
            return true;
        } else if (player.equals(possessor)) {
            return ClientUtil.MC.gameSettings.getPointOfView().func_243192_a();
        } else if (this.isDead()) {
            return this.isCompletelyDead() && !ClientUtil.isHoldingCane();
        }
        return false;
    }

    @Override
    public boolean isImmuneToExplosions() {
        return !this.isDamageable();
    }

    @Override
    public boolean isImmuneToFire() {
        // TODO: Handle this in the behavior manager
        return this.isCompletelyDead() || super.isImmuneToFire();
    }

    @Override
    public boolean canRenderOnFire() {
        // We have to render the fire separately for preview puppets to make it line up right
        return this.isAddedToWorld() && (renderManager.flaming.get() || super.canRenderOnFire());
    }

    @Override
    public PushReaction getPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean hitByEntity(Entity entity) {
        if (entity instanceof PlayerEntity) {
            final PlayerEntity player = (PlayerEntity) entity;
            final ItemStack stack = player.getHeldItemMainhand();
            if (stack.getItem() instanceof CuratorsCaneItem) {
                if (!world.isRemote) {
                    this.remove();
                }
                world.playSound(
                        null,
                        entity.getPosX(),
                        entity.getPosY(),
                        entity.getPosZ(),
                        SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK,
                        entity.getSoundCategory(),
                        1.0F,
                        1.0F);
                return true;
            }
        }
        return this.isDead() || entity.equals(possessor);
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
    public void causeLightningStrike(ServerWorld world, LightningBoltEntity lightning) {
        if (this.isDamageable()) {
            super.causeLightningStrike(world, lightning);
        }
    }

    @Override
    public boolean canBeHitWithPotion() {
        // TODO: Handle this in the behavior manager
        return this.isDamageable(); // || (invulnerable && behaviorManager.allowPotions)
    }

    @Override
    public boolean attackable() {
        return this.isDamageable();
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
        super.notifyDataManagerChange(key);
        if (key.equals(POSSESSOR_ID)) {
            final OptionalInt id = dataManager.get(POSSESSOR_ID);
            final Entity possessor = id.isPresent() ? world.getEntityByID(id.getAsInt()) : null;
            this.setPossessor(possessor);
        } else {
            // Sanity check; superclass calls this method before the managers exist
            if (managers != null) {
                for (PuppetValueManager manager : managers.values()) {
                    if (manager.onDataChanged(key)) {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return renderManager.getRenderBounds();
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        // Why doesn't vanilla use the render bounds for this...
        double d0 = this.getRenderBoundingBox().getAverageEdgeLength();
        if (Double.isNaN(d0)) {
            d0 = 1.0D;
        }
        d0 = d0 * 64.0D * renderDistanceWeight;
        return distance < d0 * d0;
    }

    @Override
    public float getRenderScale() {
        return renderManager.scale.get();
    }

    @Override
    public boolean isGlowing() {
        return super.isGlowing() || world.isRemote && ClientUtil.shouldPuppetGlow(this);
    }

    @Override
    public int getTeamColor() {
        if (ClientUtil.getSelectedPuppet() == this) {
            return 0x00FFFF;
        } else if (renderManager.canRenderHiddenDeathEffects()) {
            return 0xFF0000;
        } else {
            final GlowColor color = GlowColor.DEFAULT; // renderManager.glowColor.get();
            return color.useTeamColor ? super.getTeamColor() : color.rgb;
        }
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(animationManager.getController());
    }

    @Override
    public AnimationFactory getFactory() {
        return animationManager.factory;
    }

    public void invalidateCaches() {
        for (PuppetValueManager manager : managers.values()) {
            manager.invalidateCaches();
        }
    }

    public boolean isSitting() {
        final Entity vehicle =
                possessor != null ? possessor.getRidingEntity() : this.getRidingEntity();
        return vehicle != null && vehicle.shouldRiderSit();
    }

    @Nullable
    public Entity getPossessor() {
        return possessor;
    }

    public void setPossessor(@Nullable Entity possessor) {
        final Entity prev = this.possessor;
        if (!Objects.equals(possessor, prev)) {
            if (this.isServerWorld()) {
                final OptionalInt id =
                        possessor != null
                                ? OptionalInt.of(possessor.getEntityId())
                                : OptionalInt.empty();
                dataManager.set(POSSESSOR_ID, id);
            }
            this.possessor = possessor;
            if (possessor != null) possessor.recalculateSize();
            if (prev != null) prev.recalculateSize();
        }
    }
}
