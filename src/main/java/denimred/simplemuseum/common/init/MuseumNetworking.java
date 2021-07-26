package denimred.simplemuseum.common.init;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.network.messages.bidirectional.CopyPastePuppetData;
import denimred.simplemuseum.common.network.messages.c2s.C2SCryptMasterRemovePuppet;
import denimred.simplemuseum.common.network.messages.c2s.C2SCryptMasterSpawnPuppet;
import denimred.simplemuseum.common.network.messages.c2s.C2SMovePuppet;
import denimred.simplemuseum.common.network.messages.c2s.ConfigurePuppet;
import denimred.simplemuseum.common.network.messages.s2c.PlayPuppetAnimation;
import denimred.simplemuseum.common.network.messages.s2c.ResurrectPuppetSync;

public final class MuseumNetworking {
    public static final String NETWORK_VERSION = "3";
    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(SimpleMuseum.MOD_ID, "main"),
                    () -> NETWORK_VERSION,
                    NETWORK_VERSION::equals,
                    NETWORK_VERSION::equals);

    public static void registerMessages() {
        int id = -1;
        // Server <-- Client
        ConfigurePuppet.register(CHANNEL, ++id);
        CHANNEL.registerMessage(
                ++id,
                C2SMovePuppet.class,
                C2SMovePuppet::encode,
                C2SMovePuppet::decode,
                C2SMovePuppet::handle);
        CHANNEL.registerMessage(
                ++id,
                C2SCryptMasterSpawnPuppet.class,
                C2SCryptMasterSpawnPuppet::encode,
                C2SCryptMasterSpawnPuppet::decode,
                C2SCryptMasterSpawnPuppet::handle);
        CHANNEL.registerMessage(
                ++id,
                C2SCryptMasterRemovePuppet.class,
                C2SCryptMasterRemovePuppet::encode,
                C2SCryptMasterRemovePuppet::decode,
                C2SCryptMasterRemovePuppet::handle);
        // Server --> Client
        PlayPuppetAnimation.register(CHANNEL, ++id);
        ResurrectPuppetSync.register(CHANNEL, ++id);
        // Server <-> Client
        CopyPastePuppetData.register(CHANNEL, ++id);
    }
}
