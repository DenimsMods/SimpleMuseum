package denimred.simplemuseum.common.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.util.SharedConstants;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.entity.MuseumDummyEntity;

public final class MuseumEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ENTITIES, SimpleMuseum.MOD_ID);

    public static final RegistryObject<EntityType<MuseumDummyEntity>> MUSEUM_DUMMY =
            REGISTRY.register(
                    "museum_dummy",
                    () ->
                            makeEntityType(
                                    EntityType.Builder.create(
                                                    MuseumDummyEntity::new,
                                                    EntityClassification.MISC)
                                            .size(0.6F, 1.8F)
                                            .trackingRange(32),
                                    LivingEntity.registerAttributes().create()));

    @SuppressWarnings({
        "ConstantConditions",
        "unchecked"
    }) // We're careful, so these warnings are unneeded
    private static <T extends Entity> EntityType<T> makeEntityType(
            EntityType.Builder<T> builder, @Nullable AttributeModifierMap attributes) {
        // Thankfully we can use this property to disable datafixers for a moment while we create
        // our entity type
        final boolean udf = SharedConstants.useDatafixers;
        SharedConstants.useDatafixers = false;
        final EntityType<T> type = builder.build(null);
        SharedConstants.useDatafixers = udf;
        // We apply our entity's attributes here to make sure they're available when the entity is
        // registered later
        if (attributes != null) {
            try {
                GlobalEntityTypeAttributes.put(
                        (EntityType<? extends LivingEntity>) type, attributes);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(
                        "Tried to apply attributes to non-living entity type", e);
            }
        }
        return type;
    }
}
