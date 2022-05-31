package denimred.simplemuseum.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerChangeGameModeEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.MovementEditorClient;
import denimred.simplemuseum.client.renderer.MovementRenderer;
import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.init.MuseumItems;
import denimred.simplemuseum.common.item.CuratorsCaneItem;

@Mod.EventBusSubscriber(
        modid = SimpleMuseum.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public class ClientForgeEventHandler {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() && ClientUtil.MC.screen == null) {
            final Player player = event.player;
            if (player == ClientUtil.MC.player && !player.isSpectator()) {
                final CuratorsCaneItem cane = MuseumItems.CURATORS_CANE.get();
                final boolean holdingCane =
                        player.getMainHandItem().getItem() == cane
                                || player.getOffhandItem().getItem() == cane;
                ClientUtil.setHoldingCane(holdingCane);
                ClientUtil.selectPuppet(
                        holdingCane ? ClientUtil.getHoveredPuppet(player) : null, false);
            }
        }
    }

    @SubscribeEvent
    public static void onClientPlayerChangeGameMode(ClientPlayerChangeGameModeEvent event) {
        if (event.getNewGameMode() == GameType.SPECTATOR) {
            ClientUtil.setHoldingCane(false);
            ClientUtil.deselectPuppet(false);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END && !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null))
            MovementEditorClient.tick();
    }

    @SubscribeEvent
    public static void onInputMouseScroll(InputEvent.MouseScrollEvent event) {
        if(Screen.hasControlDown())
            if(AreaHandler.pushPull(Math.signum(event.getScrollDelta())))
                event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        MovementRenderer.render(event.getMatrixStack());
    }
}
