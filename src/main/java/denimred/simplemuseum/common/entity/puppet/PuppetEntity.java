package denimred.simplemuseum.common.entity.puppet;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Collections;
import java.util.HashMap;
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
import denimred.simplemuseum.common.item.HeldItemStack;
import denimred.simplemuseum.common.network.messages.bidirectional.SyncHeldItems;
import denimred.simplemuseum.common.network.messages.s2c.ResurrectPuppetSync;
import denimred.simplemuseum.common.util.GlowColor;
import denimred.simplemuseum.common.util.IValueSerializer;
import denimred.simplemuseum.common.util.MathUtil;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public final class PuppetEntity extends LivingEntity implements IAnimatable, IAnimationTickable {
    public static final EntityDataAccessor<OptionalInt> POSSESSOR_ID =
            SynchedEntityData.defineId(
                    PuppetEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
    // Manager initialization order is different from normal due to NPEs when managers reference
    // each other during initialization
    public final PuppetSourceManager sourceManager = new PuppetSourceManager(this);
    public final PuppetRenderManager renderManager = new PuppetRenderManager(this);
    public final PuppetAudioManager audioManager = new PuppetAudioManager(this);
    public final PuppetAnimationManager animationManager = new PuppetAnimationManager(this);
    public final PuppetBehaviorManager behaviorManager = new PuppetBehaviorManager(this);
    public final PuppetEasterEggTracker easterEggs = new PuppetEasterEggTracker(this);
    private final Map<String, PuppetValueManager> managers =
            new Object2ReferenceLinkedOpenHashMap<>();
    private int livingSoundTime;
    @Nullable private Entity possessor;
    private final HashMap<String, HeldItemStack> heldItems = new HashMap<>();

    public PuppetEntity(EntityType<? extends PuppetEntity> type, Level world) {
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
    public static <T> EntityDataAccessor<T> createKeyContextual(IValueSerializer<T> serializer) {
        final EntityDataSerializer<T> dataSerializer =
                serializer instanceof IValueSerializer.Wrapped
                        ? ((IValueSerializer.Wrapped<T>) serializer).parent
                        : serializer;
        return SynchedEntityData.defineId(PuppetEntity.class, dataSerializer);
    }

    @Nullable
    public static PuppetEntity spawn(ServerLevel world, Vec3 pos, @Nullable Entity entity) {
        return spawn(world, pos, entity != null ? entity.position() : null);
    }

    @Nullable
    public static PuppetEntity spawn(ServerLevel world, Vec3 pos, @Nullable Vec3 facing) {
        final PuppetEntity puppet = MuseumEntities.MUSEUM_PUPPET.get().create(world);
        if (puppet != null) {
            final float yaw = facing != null ? MathUtil.yawBetween(pos, facing) : 0.0F;
            puppet.moveTo(pos.x, pos.y, pos.z, yaw, 0.0F);
            puppet.yHeadRot = yaw;
            world.addFreshEntityWithPassengers(puppet);
        }
        return puppet;
    }

    public static PuppetEntity makePreviewCopy(PuppetEntity original) {
        final PuppetEntity copy =
                Objects.requireNonNull(MuseumEntities.MUSEUM_PUPPET.get().create(original.level));
        copy.setId(original.getId());
        copy.setCustomName(original.getCustomName());
        copy.yHeadRot = 0.0F;
        copy.yRot = 0.0F;
        copy.xRot = 0.0F;
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
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(POSSESSOR_ID, OptionalInt.empty());
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
    public void refreshDimensions() {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        super.refreshDimensions();
        this.setPos(x, y, z);
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn) {
        // TODO: Change for different poses?
        // Note: Not adjusting based on render scale for performance reasons
        return behaviorManager.physicalSize.get();
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (this.isAlive() && random.nextInt(1000) < livingSoundTime++) {
            livingSoundTime -= 80; // TODO: Make this configurable
            audioManager.playAmbientSound();
        }
        if (this.level.isClientSide) easterEggs.tick();
    }

    @Override
    public SoundSource getSoundSource() {
        return audioManager.category.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        final CompoundTag modTag = tag.getCompound(SimpleMuseum.MOD_ID);
        this.writeModTag(modTag);
        if (!modTag.isEmpty()) {
            tag.put(SimpleMuseum.MOD_ID, modTag);
        }
    }

    public void writeModTag(CompoundTag modTag) {
        PuppetDataHistorian.writeVersion(modTag);
        for (PuppetValueManager manager : managers.values()) {
            manager.write(modTag);
        }

        ListTag heldItemsTag = new ListTag();
        for(Map.Entry<String, HeldItemStack> pair : heldItems.entrySet()) {
            CompoundTag pairTag = new CompoundTag();
            pairTag.putString(HeldItemStack.NBT_BONENAME, pair.getKey());
            pairTag.put(HeldItemStack.NBT_HELDITEM, pair.getValue().serializeNBT());
            heldItemsTag.add(pairTag);
        }
        modTag.put(HeldItemStack.NBT_LIST, heldItemsTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readModTag(tag.getCompound(SimpleMuseum.MOD_ID));
    }

    public void readModTag(CompoundTag modTag) {
        PuppetDataHistorian.checkAndUpdate(modTag);
        for (PuppetValueManager manager : managers.values()) {
            manager.read(modTag);
        }
        ListTag heldItemsTag = modTag.getList(HeldItemStack.NBT_LIST, Constants.NBT.TAG_COMPOUND);
        HashMap<String, HeldItemStack> map = new HashMap<>();
        for(Tag tag : heldItemsTag) {
            CompoundTag heldItemTag = (CompoundTag) tag;
            map.put(heldItemTag.getString(HeldItemStack.NBT_BONENAME), HeldItemStack.deserializeNBT(heldItemTag.getCompound(HeldItemStack.NBT_HELDITEM)));
        }
        this.setHeldItems(map);
    }

    @Override
    public ItemStack getPickedResult(HitResult target) {
        return new ItemStack(MuseumItems.CURATORS_CANE.get());
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entityIn) {}

    @Override
    protected void pushEntities() {}

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return (!this.isDamageable()) && source != DamageSource.OUT_OF_WORLD;
    }

    @Override
    public boolean isPickable() {
        return level.isClientSide && ClientUtil.isClientPossessing(this)
                ? ClientUtil.isHoldingCane()
                : super.isPickable();
    }

    @Override
    protected float tickHeadTurn(float yawOffset, float what) {
        if (possessor == null) {
            yBodyRotO = yRotO;
            yBodyRot = yRot;
            return 0.0F;
        } else {
            this.yBodyRot += Mth.wrapDegrees(yawOffset - this.yBodyRot) * 0.3F;
            float yaw = Mth.wrapDegrees(this.yRot - this.yBodyRot);
            boolean flag = yaw < -90.0F || yaw >= 90.0F;
            if (yaw < -75.0F) {
                yaw = -75.0F;
            }

            if (yaw >= 75.0F) {
                yaw = 75.0F;
            }

            this.yBodyRot = this.yRot - yaw;
            if (yaw * yaw > 2500.0F) {
                this.yBodyRot += yaw * 0.2F;
            }

            if (flag) {
                what *= -1.0F;
            }
            return what;
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        // TODO: Make the boolean here configurable (true to include Y value in limb swing calc)
        this.calculateEntityAnimation(this, false);
    }

    @Override
    public void setYBodyRot(float offset) {
        yBodyRotO = yRotO = offset;
        yHeadRotO = yHeadRot = offset;
    }

    @Override
    public void setYHeadRot(float rotation) {
        yBodyRotO = yRotO = rotation;
        yHeadRotO = yHeadRot = rotation;
    }

    @Override
    public void kill() {
        if (!this.isDamageable()) {
            this.remove();
        } else {
            super.kill();
        }
    }

    @Override
    public void die(DamageSource cause) {
        if (this.isCompletelyDead() && cause == DamageSource.OUT_OF_WORLD) {
            this.remove();
        } else {
            super.die(cause);
        }
    }

    @Override
    protected void tickDeath() {
        ++deathTime;
        if (deathTime == animationManager.deathLength.get()) {
            for (int i = 0; i < 20; ++i) {
                level.addParticle(
                        ParticleTypes.POOF,
                        this.getRandomX(1.0D),
                        this.getRandomY(),
                        this.getRandomZ(1.0D),
                        random.nextGaussian() * 0.02D,
                        random.nextGaussian() * 0.02D,
                        random.nextGaussian() * 0.02D);
            }
        }
    }

    public void resurrect() {
        this.setHealth(this.getMaxHealth());
        dead = false;
        deathTime = 0;
        if (!level.isClientSide) {
            MuseumNetworking.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY.with(() -> this),
                    new ResurrectPuppetSync(this.getId()));
        }
    }

    public boolean exists() {
        //noinspection deprecation
        return !removed;
    }

    public boolean isDead() {
        return dead || deathTime > 0 || this.isDeadOrDying();
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
    public boolean isInvisibleTo(Player player) {
        if (super.isInvisibleTo(player)) {
            return true;
        } else if (player.equals(possessor)) {
            return ClientUtil.MC.options.getCameraType().isFirstPerson();
        } else if (this.isDead()) {
            return this.isCompletelyDead() && !ClientUtil.isHoldingCane();
        }
        return false;
    }

    @Override
    public boolean ignoreExplosion() {
        return !this.isDamageable();
    }

    @Override
    public boolean fireImmune() {
        // TODO: Handle this in the behavior manager
        return this.isCompletelyDead() || super.fireImmune();
    }

    @Override
    public boolean displayFireAnimation() {
        // We have to render the fire separately for preview puppets to make it line up right
        return this.isAddedToWorld()
                && (renderManager.flaming.get() || super.displayFireAnimation());
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        if (entity instanceof Player) {
            final Player player = (Player) entity;
            final ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof CuratorsCaneItem) {
                if (!level.isClientSide) {
                    this.remove();
                }
                level.playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        SoundEvents.PLAYER_ATTACK_KNOCKBACK,
                        entity.getSoundSource(),
                        1.0F,
                        1.0F);
                return true;
            }
            CompoundTag tag = new CompoundTag();
            save(tag);
            System.out.println(tag);
        }
        return this.isDead() || entity.equals(possessor);
    }

    @Override
    protected SoundEvent getFallDamageSound(int heightIn) {
        // TODO: Make this configurable
        return SoundEvents.ARMOR_STAND_FALL;
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
    public void thunderHit(ServerLevel world, LightningBolt lightning) {
        if (this.isDamageable()) {
            super.thunderHit(world, lightning);
        }
    }

    @Override
    public boolean isAffectedByPotions() {
        // TODO: Handle this in the behavior manager
        return this.isDamageable(); // || (invulnerable && behaviorManager.allowPotions)
    }

    @Override
    public boolean attackable() {
        return this.isDamageable();
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slotIn) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slotIn, ItemStack stack) {}

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key.equals(POSSESSOR_ID)) {
            final OptionalInt id = entityData.get(POSSESSOR_ID);
            final Entity possessor = id.isPresent() ? level.getEntity(id.getAsInt()) : null;
            this.setPossessor(possessor);
        }
        else {
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
    public AABB getBoundingBoxForCulling() {
        return renderManager.getRenderBounds();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        // Why doesn't vanilla use the render bounds for this...
        double d0 = this.getBoundingBoxForCulling().getSize();
        if (Double.isNaN(d0)) {
            d0 = 1.0D;
        }
        d0 = d0 * 64.0D * viewScale;
        return distance < d0 * d0;
    }

    @Override
    public float getScale() {
        return renderManager.scale.get();
    }

    @Override
    public boolean isGlowing() {
        return super.isGlowing() || level.isClientSide && ClientUtil.shouldPuppetGlow(this);
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
        final Entity vehicle = possessor != null ? possessor.getVehicle() : this.getVehicle();
        return vehicle != null && vehicle.shouldRiderSit();
    }

    @Nullable
    public Entity getPossessor() {
        return possessor;
    }

    public void setPossessor(@Nullable Entity possessor) {
        final Entity prev = this.possessor;
        if (!Objects.equals(possessor, prev)) {
            if (this.isEffectiveAi()) {
                final OptionalInt id =
                        possessor != null ? OptionalInt.of(possessor.getId()) : OptionalInt.empty();
                entityData.set(POSSESSOR_ID, id);
            }
            this.possessor = possessor;
            if (possessor != null) possessor.refreshDimensions();
            if (prev != null) prev.refreshDimensions();
        }
    }

    public HeldItemStack getHeldItem(String bone) {
        return heldItems.get(bone);
    }

    public HashMap<String, HeldItemStack> getHeldItems() {
        return heldItems;
    }

    public void setHeldItems(HashMap<String, HeldItemStack> map) {
        this.heldItems.clear();
        this.heldItems.putAll(map);
        if(!level.isClientSide) {
            MuseumNetworking.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY.with(() -> this),
                    new SyncHeldItems(getId(), getHeldItems()));
        }
    }

    public void setHeldItem(String bone, HeldItemStack itemStack) {
        heldItems.put(bone, itemStack);
    }

    public void removeHeldItem(String bone) {
        heldItems.remove(bone);
    }

    @Override
    public int tickTimer() {
        return tickCount;
    }
}
