package denimred.simplemuseum.client.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.renderer.entity.PuppetRenderer;
import denimred.simplemuseum.common.init.MuseumEntities;
import denimred.simplemuseum.common.init.MuseumKeybinds;

@Mod.EventBusSubscriber(
        modid = SimpleMuseum.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class ClientModEventHandler {
    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(MuseumKeybinds.GLOBAL_HIGHLIGHTS);
    }

    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MuseumEntities.MUSEUM_PUPPET.get(), PuppetRenderer::new);
    }
}
