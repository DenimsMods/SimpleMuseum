package denimred.simplemuseum.common.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.command.PuppetCommand;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.init.MuseumEntities;
import denimred.simplemuseum.common.init.MuseumItems;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.messages.bidirectional.SyncHeldItems;
import denimred.simplemuseum.common.network.messages.s2c.PlayPuppetAnimation;

@Mod.EventBusSubscriber(modid = SimpleMuseum.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeEventHandler {
    @SubscribeEvent
    public static void onItemRegistryMissingMappings(RegistryEvent.MissingMappings<Item> event) {
        for (RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getAllMappings()) {
            if (mapping.key.equals(
                    new ResourceLocation(SimpleMuseum.MOD_ID, "museum_dummy_spawn_egg"))) {
                mapping.remap(MuseumItems.CURATORS_CANE.get());
                break; // Don't forget to remove this if we remap anything else
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTypeRegistryMissingMappings(
            RegistryEvent.MissingMappings<EntityType<?>> event) {
        for (RegistryEvent.MissingMappings.Mapping<EntityType<?>> mapping :
                event.getAllMappings()) {
            if (mapping.key.equals(new ResourceLocation(SimpleMuseum.MOD_ID, "museum_dummy"))) {
                mapping.remap(MuseumEntities.MUSEUM_PUPPET.get());
                break; // Don't forget to remove this if we remap anything else
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(PuppetCommand.create());
    }

    @SubscribeEvent
    public static void onEntityStartTracking(PlayerEvent.StartTracking event) {
        if(event.getTarget() instanceof PuppetEntity) {
            PuppetEntity puppet = (PuppetEntity) event.getTarget();
            MuseumNetworking.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY.with(() -> puppet),
                    new SyncHeldItems(puppet.getId(), puppet.getHeldItems()));
        }
    }
}
