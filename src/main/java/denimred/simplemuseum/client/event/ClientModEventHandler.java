package denimred.simplemuseum.client.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.renderer.entity.MuseumDummyRenderer;
import denimred.simplemuseum.common.init.MuseumEntities;

@Mod.EventBusSubscriber(
        modid = SimpleMuseum.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public class ClientModEventHandler {
    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(
                MuseumEntities.MUSEUM_DUMMY.get(), MuseumDummyRenderer::new);
    }
}
