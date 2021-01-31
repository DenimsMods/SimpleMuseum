package denimred.simplemuseum.common.init;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.network.messages.c2s.C2SConfigureDummy;
import denimred.simplemuseum.common.network.messages.c2s.C2SCryptMasterRemoveDummy;
import denimred.simplemuseum.common.network.messages.c2s.C2SCryptMasterSpawnDummy;
import denimred.simplemuseum.common.network.messages.c2s.C2SMoveDummy;

public final class MuseumNetworking {
    private static final String PROTOCOL_VERSION = "2";
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
        CHANNEL.registerMessage(
                ++id,
                C2SMoveDummy.class,
                C2SMoveDummy::encode,
                C2SMoveDummy::decode,
                C2SMoveDummy::handle);
        CHANNEL.registerMessage(
                ++id,
                C2SCryptMasterSpawnDummy.class,
                C2SCryptMasterSpawnDummy::encode,
                C2SCryptMasterSpawnDummy::decode,
                C2SCryptMasterSpawnDummy::handle);
        CHANNEL.registerMessage(
                ++id,
                C2SCryptMasterRemoveDummy.class,
                C2SCryptMasterRemoveDummy::encode,
                C2SCryptMasterRemoveDummy::decode,
                C2SCryptMasterRemoveDummy::handle);
    }
}
