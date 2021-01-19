package denimred.simplemuseum.client.event;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.renderer.entity.MuseumDummyRenderer;
import denimred.simplemuseum.common.init.MuseumEntities;
import denimred.simplemuseum.common.init.MuseumItems;
import denimred.simplemuseum.common.item.DeferredSpawnEgg;

@Mod.EventBusSubscriber(
        modid = SimpleMuseum.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public class ClientModEventHandler {
    public static final IItemColor EGG_COLOR =
            (stack, tintIndex) -> ((DeferredSpawnEgg) stack.getItem()).getColor(tintIndex);

    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(
                MuseumEntities.MUSEUM_DUMMY.get(), MuseumDummyRenderer::new);
    }

    @SubscribeEvent
    public static void onColorHandlerItem(ColorHandlerEvent.Item event) {
        final ItemColors itemColors = event.getItemColors();
        itemColors.register(EGG_COLOR, MuseumItems.MUSEUM_DUMMY_SPAWN_EGG.get());
    }
}
