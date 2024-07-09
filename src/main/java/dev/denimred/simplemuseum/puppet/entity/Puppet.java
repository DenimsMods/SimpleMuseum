package dev.denimred.simplemuseum.puppet.entity;

import dev.denimred.simplemuseum.puppet.PuppetContext;
import dev.denimred.simplemuseum.puppet.data.PuppetFacetStore;
import dev.denimred.simplemuseum.puppet.data.SyncPuppetEntityFacets;
import dev.denimred.simplemuseum.puppet.edit.OpenPuppetEditScreen;
import dev.denimred.simplemuseum.puppet.edit.PuppetEditMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import static dev.denimred.simplemuseum.SimpleMuseum.LOGGER;

public class Puppet extends PathfinderMob implements GeoEntity, PuppetContext {
    public static final String FACETS_TAG = "facets";
    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);
    private final PuppetFacetStore facets = new PuppetFacetStore();

    public Puppet(EntityType<? extends Puppet> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes();
    }

    public static Puppet getPuppet(Level level, int id, String context, EnvType env) {
        var entity = level.getEntity(id);
        if (entity instanceof Puppet puppet) return puppet;
        var envName = env == EnvType.CLIENT ? "Client" : "Server";
        if (entity == null) throw new NullPointerException(envName + "tried to " + context + " for null entity");
        throw new IllegalArgumentException(envName + " tried to " + context + " for non-puppet entity");
    }

    public PuppetFacetStore facets() {
        return facets;
    }

    @Override
    public boolean isValid() {
        return !isRemoved();
    }

    @Override
    public FabricPacket createOpenMenuPacket(PuppetEditMenu menu) {
        return new OpenPuppetEditScreen(menu, this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animCache;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag root) {
        super.addAdditionalSaveData(root);
        LOGGER.trace("Saving puppet #{} facets", getId());
        var facetsTag = facets.save();
        if (!facetsTag.isEmpty()) root.put(FACETS_TAG, facetsTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag root) {
        super.readAdditionalSaveData(root);
        LOGGER.trace("Loading puppet #{} facets", getId());
        facets.load(root.getCompound(FACETS_TAG));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        LOGGER.trace("Syncing puppet #{} facets to {} ({})", getId(), player.getGameProfile().getName(), player.getStringUUID());
        ServerPlayNetworking.send(player, new SyncPuppetEntityFacets(this, facets.getAllInstances()));
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }
}
