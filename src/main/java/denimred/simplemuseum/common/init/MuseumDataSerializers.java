package denimred.simplemuseum.common.init;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.Color;
import java.util.function.Supplier;

import denimred.simplemuseum.SimpleMuseum;

public final class MuseumDataSerializers {
    public static final DeferredRegister<DataSerializerEntry> REGISTRY =
            DeferredRegister.create(ForgeRegistries.DATA_SERIALIZERS, SimpleMuseum.MOD_ID);

    public static final RegistryObject<DataSerializerEntry> INTEGER =
            REGISTRY.register(
                    "integer",
                    () ->
                            new DataSerializerEntry(
                                    new IDataSerializer<Integer>() {
                                        public void write(PacketBuffer buf, Integer value) {
                                            buf.writeInt(value);
                                        }

                                        public Integer read(PacketBuffer buf) {
                                            return buf.readInt();
                                        }

                                        public Integer copyValue(Integer value) {
                                            return value;
                                        }
                                    }));
    public static final RegistryObject<DataSerializerEntry> RESOURCE_LOCATION =
            REGISTRY.register(
                    "resource_location",
                    () ->
                            new DataSerializerEntry(
                                    new IDataSerializer<ResourceLocation>() {
                                        public void write(
                                                PacketBuffer buf, ResourceLocation value) {
                                            buf.writeString(value.toString());
                                        }

                                        public ResourceLocation read(PacketBuffer buf) {
                                            return new ResourceLocation(buf.readString(32767));
                                        }

                                        public ResourceLocation copyValue(ResourceLocation value) {
                                            return new ResourceLocation(
                                                    value.getNamespace(), value.getPath());
                                        }
                                    }));
    public static final RegistryObject<DataSerializerEntry> COLOR =
            REGISTRY.register(
                    "color",
                    () ->
                            new DataSerializerEntry(
                                    new IDataSerializer<Color>() {
                                        public void write(PacketBuffer buf, Color value) {
                                            buf.writeInt(value.getRGB());
                                        }

                                        public Color read(PacketBuffer buf) {
                                            return new Color(buf.readInt(), true);
                                        }

                                        public Color copyValue(Color value) {
                                            return value;
                                        }
                                    }));
    public static final RegistryObject<DataSerializerEntry> SOUND_CATEGORY =
            REGISTRY.register(
                    "sound_category",
                    () ->
                            new DataSerializerEntry(
                                    new IDataSerializer<SoundCategory>() {
                                        public void write(PacketBuffer buf, SoundCategory value) {
                                            buf.writeEnumValue(value);
                                        }

                                        public SoundCategory read(PacketBuffer buf) {
                                            return buf.readEnumValue(SoundCategory.class);
                                        }

                                        public SoundCategory copyValue(SoundCategory value) {
                                            return value;
                                        }
                                    }));

    public static IDataSerializer<Integer> integer() {
        return getUnchecked(MuseumDataSerializers.INTEGER);
    }

    public static IDataSerializer<ResourceLocation> resourceLocation() {
        return getUnchecked(MuseumDataSerializers.RESOURCE_LOCATION);
    }

    public static IDataSerializer<Color> color() {
        return getUnchecked(MuseumDataSerializers.COLOR);
    }

    public static IDataSerializer<SoundCategory> soundCategory() {
        return getUnchecked(MuseumDataSerializers.SOUND_CATEGORY);
    }

    // Forge doesn't generify this "due to restrictions in how the registry system works"
    // See Forge PR #6601 for more information
    @SuppressWarnings("unchecked")
    private static <T> IDataSerializer<T> getUnchecked(Supplier<DataSerializerEntry> entry) {
        return (IDataSerializer<T>) entry.get().getSerializer();
    }
}
