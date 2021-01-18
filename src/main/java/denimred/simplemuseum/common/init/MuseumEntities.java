package denimred.simplemuseum.common.init;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.util.SharedConstants;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.entity.MuseumDummyEntity;

public class MuseumEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ENTITIES, SimpleMuseum.MOD_ID);

    public static final RegistryObject<EntityType<MuseumDummyEntity>> MUSEUM_DUMMY =
            REGISTRY.register(
                    "museum_dummy",
                    () -> {
                        final boolean b = SharedConstants.useDatafixers;
                        SharedConstants.useDatafixers = false;
                        //noinspection ConstantConditions
                        final EntityType<MuseumDummyEntity> type =
                                EntityType.Builder.create(
                                                MuseumDummyEntity::new, EntityClassification.MISC)
                                        .size(0.6F, 1.8F)
                                        .trackingRange(32)
                                        .build(null);
                        SharedConstants.useDatafixers = b;
                        GlobalEntityTypeAttributes.put(
                                type, LivingEntity.registerAttributes().create());
                        return type;
                    });
}
