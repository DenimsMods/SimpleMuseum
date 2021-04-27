package denimred.simplemuseum.common.event;

import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.init.MuseumCommands;
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
        MuseumCommands.register(event.getDispatcher());
    }
}
