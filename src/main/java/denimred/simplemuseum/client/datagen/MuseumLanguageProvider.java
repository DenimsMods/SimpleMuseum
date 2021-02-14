package denimred.simplemuseum.client.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import cryptcraft.cryptmaster.plugin.client.UtilityTool;
import denimred.simplemuseum.common.init.MuseumEntities;
import denimred.simplemuseum.common.init.MuseumItems;
import denimred.simplemuseum.common.init.MuseumLang;
import denimred.simplemuseum.modcompat.cryptmaster.MuseumTool;

public class MuseumLanguageProvider extends LanguageProvider {
    public MuseumLanguageProvider(DataGenerator gen, String modId, String locale) {
        super(gen, modId, locale);
    }

    @Override
    protected void addTranslations() {
        // Lang for registry objects
        this.addEntityType(MuseumEntities.MUSEUM_DUMMY, "Museum Dummy");
        this.addItem(MuseumItems.CURATORS_CANE, "Curator's Cane");
        // Misc lang
        MuseumLang.provideFor(this);
        // Mod compat lang (datagen is done in dev, no need to worry about classloading)
        this.addCryptMasterTool(MuseumTool.INSTANCE, "Create/Edit Museum Dummy");
    }

    @SuppressWarnings("SameParameterValue")
    private void addCryptMasterTool(UtilityTool tool, String name) {
        this.add(tool.getTooltip().getKey(), name);
    }
}
