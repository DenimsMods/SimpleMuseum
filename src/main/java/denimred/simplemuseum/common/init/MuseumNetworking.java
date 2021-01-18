package denimred.simplemuseum.common.init;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.network.messages.c2s.C2SConfigureDummy;

public class MuseumNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(SimpleMuseum.MOD_ID, "main"),
                    () -> PROTOCOL_VERSION,
                    PROTOCOL_VERSION::equals,
                    PROTOCOL_VERSION::equals);

    public static void registerMessages() {
        int id = -1;
        // Client -> Server
        CHANNEL.registerMessage(
                ++id,
                C2SConfigureDummy.class,
                C2SConfigureDummy::encode,
                C2SConfigureDummy::decode,
                C2SConfigureDummy::handle);
    }
}
