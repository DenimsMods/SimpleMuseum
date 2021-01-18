package denimred.simplemuseum.client.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import denimred.simplemuseum.common.init.MuseumEntities;
import denimred.simplemuseum.common.init.MuseumItems;

public class MuseumLanguageProvider extends LanguageProvider {
    public MuseumLanguageProvider(DataGenerator gen, String modId, String locale) {
        super(gen, modId, locale);
    }

    @Override
    protected void addTranslations() {
        this.addEntityType(MuseumEntities.MUSEUM_DUMMY, "Museum Dummy");
        this.addItem(MuseumItems.MUSEUM_DUMMY_SPAWN_EGG, "Museum Dummy Spawn Egg");
    }
}
