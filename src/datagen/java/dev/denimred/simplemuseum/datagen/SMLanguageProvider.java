package dev.denimred.simplemuseum.datagen;

import dev.denimred.simplemuseum.init.SMEntityTypes;
import dev.denimred.simplemuseum.init.SMItems;
import dev.denimred.simplemuseum.puppet.PuppetCommands;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

final class SMLanguageProvider extends FabricLanguageProvider {
    SMLanguageProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateTranslations(TranslationBuilder gen) {
        gen.add(SMItems.CURATORS_CANE, "Curator's Cane");
        gen.add(SMEntityTypes.PUPPET, "Puppet");
        gen.add(PuppetCommands.SET_SINGLE, "Set %s to %s for %d puppet");
        gen.add(PuppetCommands.SET_MULTI, "Set %s to %s for %d puppets");
        gen.add(PuppetCommands.PUPPET_SELECTOR, "Nearest puppet");
        gen.add(PuppetCommands.PUPPETS_SELECTOR, "All puppets");
    }
}
