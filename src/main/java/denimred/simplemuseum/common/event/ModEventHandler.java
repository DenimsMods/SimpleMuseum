package denimred.simplemuseum.common.event;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.datagen.MuseumItemModelProvider;
import denimred.simplemuseum.client.datagen.MuseumLanguageProvider;
import denimred.simplemuseum.client.datagen.MuseumTextureProvider;
import denimred.simplemuseum.common.init.MuseumEntities;

@Mod.EventBusSubscriber(modid = SimpleMuseum.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEventHandler {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        final DataGenerator gen = event.getGenerator();
        final String modId = event.getModContainer().getModId();
        final ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        if (event.includeClient()) {
            gen.addProvider(new MuseumLanguageProvider(gen, modId, "en_us"));
            gen.addProvider(new MuseumItemModelProvider(gen, modId, existingFileHelper));
            gen.addProvider(new MuseumTextureProvider(gen, modId));
        }
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(
                MuseumEntities.MUSEUM_PUPPET.get(), LivingEntity.createLivingAttributes().build());
    }
}
