package denimred.simplemuseum.client.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerChangeGameModeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import denimred.simplemuseum.SimpleMuseum;
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
        if (event.side.isClient() && ClientUtil.MC.currentScreen == null) {
            final PlayerEntity player = event.player;
            if (player == ClientUtil.MC.player && !player.isSpectator()) {
                final CuratorsCaneItem cane = MuseumItems.CURATORS_CANE.get();
                final boolean holdingCane =
                        player.getHeldItemMainhand().getItem() == cane
                                || player.getHeldItemOffhand().getItem() == cane;
                ClientUtil.setHoldingCane(holdingCane);
                ClientUtil.selectDummy(
                        holdingCane ? ClientUtil.getHoveredDummy(player) : null, false);
            }
        }
    }

    @SubscribeEvent
    public static void onClientPlayerChangeGameMode(ClientPlayerChangeGameModeEvent event) {
        if (event.getNewGameMode() == GameType.SPECTATOR) {
            ClientUtil.setHoldingCane(false);
            ClientUtil.deselectDummy(false);
        }
    }
}
