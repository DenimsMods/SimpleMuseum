package denimred.simplemuseum.common.event;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.datagen.MuseumItemModelProvider;
import denimred.simplemuseum.client.datagen.MuseumLanguageProvider;

@Mod.EventBusSubscriber(modid = SimpleMuseum.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        final DataGenerator gen = event.getGenerator();
        final String modId = event.getModContainer().getModId();
        final ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        if (event.includeClient()) {
            gen.addProvider(new MuseumLanguageProvider(gen, modId, "en_us"));
            gen.addProvider(new MuseumItemModelProvider(gen, modId, existingFileHelper));
        }
    }
}
