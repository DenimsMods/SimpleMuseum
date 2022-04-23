package denimred.simplemuseum.common.init;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetRenderManager;
import denimred.simplemuseum.common.util.ValueSerializers;

@SuppressWarnings("unused")
public final class MuseumDataSerializers {
    public static final DeferredRegister<DataSerializerEntry> REGISTRY =
            DeferredRegister.create(ForgeRegistries.DATA_SERIALIZERS, SimpleMuseum.MOD_ID);

    public static final RegistryObject<DataSerializerEntry> INTEGER =
            REGISTRY.register("integer", () -> new DataSerializerEntry(ValueSerializers.INTEGER));
    public static final RegistryObject<DataSerializerEntry> RESOURCE_LOCATION =
            REGISTRY.register(
                    "resource_location",
                    () -> new DataSerializerEntry(ValueSerializers.RESOURCE_LOCATION));
    public static final RegistryObject<DataSerializerEntry> COLOR =
            REGISTRY.register("color", () -> new DataSerializerEntry(ValueSerializers.COLOR));
    public static final RegistryObject<DataSerializerEntry> SOUND_CATEGORY =
            REGISTRY.register(
                    "sound_category",
                    () -> new DataSerializerEntry(ValueSerializers.SOUND_CATEGORY));
    public static final RegistryObject<DataSerializerEntry> ENTITY_SIZE =
            REGISTRY.register(
                    "entity_size", () -> new DataSerializerEntry(ValueSerializers.ENTITY_SIZE));
    public static final RegistryObject<DataSerializerEntry> BEHAVIOR_TREE =
            REGISTRY.register(
                    "behavior_tree", () -> new DataSerializerEntry(ValueSerializers.GOAL_TREE));
    public static final RegistryObject<DataSerializerEntry> GLOW_COLOR =
            REGISTRY.register(
                    "glow_color", () -> new DataSerializerEntry(ValueSerializers.GLOW_COLOR));
    public static final RegistryObject<DataSerializerEntry> RENDER_LAYER =
            REGISTRY.register(
                    "render_layer",
                    () -> new DataSerializerEntry(PuppetRenderManager.RenderLayer.SERIALIZER));
    public static final RegistryObject<DataSerializerEntry> NAMEPLATE_BEHAVIOR =
            REGISTRY.register(
                    "nameplate_behavior",
                    () ->
                            new DataSerializerEntry(
                                    PuppetRenderManager.NameplateBehavior.SERIALIZER));
}
