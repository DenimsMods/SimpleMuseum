package denimred.simplemuseum.common.init;

import static denimred.simplemuseum.common.entity.puppet.manager.PuppetBehaviorManager.PHYSICAL_SIZE;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import net.minecraftforge.registries.RegistryObject;

public final class MuseumEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ENTITIES, SimpleMuseum.MOD_ID);

    public static final RegistryObject<EntityType<PuppetEntity>> MUSEUM_PUPPET =
            register(
                    "museum_dummy", // TODO: Change to museum_puppet after CnC showcase
                    EntityType.Builder.of(PuppetEntity::new, MobCategory.MISC)
                            .sized(
                                    PHYSICAL_SIZE.defaultValue.width,
                                    PHYSICAL_SIZE.defaultValue.height)
                            .clientTrackingRange(32));

    private static <T extends Entity> RegistryObject<EntityType<T>> register(
            @SuppressWarnings("SameParameterValue") String id, EntityType.Builder<T> builder) {
        return REGISTRY.register(id, () -> builder.build(id));
    }
}
