package denimred.simplemuseum.common.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;

import static denimred.simplemuseum.common.entity.puppet.manager.PuppetBehaviorManager.PHYSICAL_SIZE;

public final class MuseumEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ENTITIES, SimpleMuseum.MOD_ID);

    public static final RegistryObject<EntityType<PuppetEntity>> MUSEUM_PUPPET =
            register(
                    "museum_dummy", // TODO: Change to museum_puppet after CnC showcase
                    EntityType.Builder.create(PuppetEntity::new, EntityClassification.MISC)
                            .size(
                                    PHYSICAL_SIZE.defaultValue.width,
                                    PHYSICAL_SIZE.defaultValue.height)
                            .trackingRange(32));

    private static <T extends Entity> RegistryObject<EntityType<T>> register(
            @SuppressWarnings("SameParameterValue") String id, EntityType.Builder<T> builder) {
        return REGISTRY.register(id, () -> builder.build(id));
    }
}
