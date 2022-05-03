package denimred.simplemuseum.common.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.command.PuppetCommand;
import denimred.simplemuseum.common.entity.puppet.goals.movement.MovementData;
import denimred.simplemuseum.common.init.MuseumEntities;
import denimred.simplemuseum.common.init.MuseumItems;

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
    public static void onWorldLoad(WorldEvent.Load event) {
        if(event.getWorld().isClientSide())
            return;
        ((ServerLevel)event.getWorld()).getDataStorage().computeIfAbsent(MovementData::new, MovementData.ID);
    }
}
