package dev.denimred.simplemuseum.puppet;

import dev.denimred.simplemuseum.puppet.data.PuppetFacetStore;
import dev.denimred.simplemuseum.puppet.data.SyncPuppetFacets;
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

public class Puppet extends PathfinderMob implements GeoEntity {
    public static final String FACETS_TAG = "facets";
    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);
    private final PuppetFacetStore facets = new PuppetFacetStore();

    public Puppet(EntityType<? extends Puppet> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes();
    }

    public PuppetFacetStore facets() {
        return facets;
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
        ServerPlayNetworking.send(player, new SyncPuppetFacets(this, facets.getAllInstances()));
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }
}
