package denimred.simplemuseum.common.init;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import denimred.simplemuseum.SimpleMuseum;

public final class MuseumDataSerializers {
    public static final DeferredRegister<DataSerializerEntry> REGISTRY =
            DeferredRegister.create(ForgeRegistries.DATA_SERIALIZERS, SimpleMuseum.MOD_ID);
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

    @SuppressWarnings("unchecked") // Forge >:I
    public static IDataSerializer<ResourceLocation> getResourceLocationSerializer() {
        return (IDataSerializer<ResourceLocation>)
                MuseumDataSerializers.RESOURCE_LOCATION.get().getSerializer();
    }
}
