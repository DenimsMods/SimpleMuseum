package denimred.simplemuseum.common.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.entity.MuseumPuppetEntity;

public final class MuseumEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ENTITIES, SimpleMuseum.MOD_ID);

    public static final RegistryObject<EntityType<MuseumPuppetEntity>> MUSEUM_PUPPET =
            register(
                    "museum_puppet",
                    EntityType.Builder.create(MuseumPuppetEntity::new, EntityClassification.MISC)
                            .size(0.6F, 1.8F)
                            .trackingRange(32));

    private static <T extends Entity> RegistryObject<EntityType<T>> register(
            @SuppressWarnings("SameParameterValue") String id, EntityType.Builder<T> builder) {
        return REGISTRY.register(id, () -> builder.build(id));
    }
}
