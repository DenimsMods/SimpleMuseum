package denimred.simplemuseum;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import denimred.simplemuseum.client.util.ResourceUtil;
import denimred.simplemuseum.common.init.MuseumDataSerializers;
import denimred.simplemuseum.common.init.MuseumEntities;
import denimred.simplemuseum.common.init.MuseumItems;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.modcompat.ModCompatUtil;

@Mod(SimpleMuseum.MOD_ID)
public final class SimpleMuseum {
    public static final String MOD_ID = "simplemuseum";
    public static final Logger LOGGER = LogManager.getLogger();

    public SimpleMuseum() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        MuseumDataSerializers.REGISTRY.register(bus);
        MuseumEntities.REGISTRY.register(bus);
        MuseumItems.REGISTRY.register(bus);
        bus.addListener(ModCompatUtil::enqueueIMC);

        MuseumNetworking.registerMessages();

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ResourceUtil::registerResourceReloadListener);
    }
}
