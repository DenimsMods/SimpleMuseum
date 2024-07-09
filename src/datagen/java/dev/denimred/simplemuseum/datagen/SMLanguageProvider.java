package dev.denimred.simplemuseum.datagen;

import dev.denimred.simplemuseum.init.SMEntityTypes;
import dev.denimred.simplemuseum.init.SMItems;
import dev.denimred.simplemuseum.init.SMPuppetFacetGroups;
import dev.denimred.simplemuseum.init.SMPuppetFacets;
import dev.denimred.simplemuseum.puppet.PuppetCommands;
import dev.denimred.simplemuseum.util.Descriptive;
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

        add(gen, SMPuppetFacetGroups.BLANK, "<BLANK>");

        add(gen, SMPuppetFacetGroups.RENDERING, "Rendering");
        add(gen, SMPuppetFacetGroups.RENDERING, "general", "General");
        add(gen, SMPuppetFacets.MODEL, "Model");
        add(gen, SMPuppetFacets.TEXTURE, "Texture");
        add(gen, SMPuppetFacets.ANIMATIONS, "Animations");

        gen.add(PuppetCommands.SET_SINGLE, "Set %s to %s for %d puppet");
        gen.add(PuppetCommands.SET_MULTI, "Set %s to %s for %d puppets");
        gen.add(PuppetCommands.PUPPET_SELECTOR, "Nearest puppet");
        gen.add(PuppetCommands.PUPPETS_SELECTOR, "All puppets");
    }

    private void add(TranslationBuilder gen, Descriptive entry, String english) {
        gen.add(entry.getDescriptionId(), english);
    }

    @SuppressWarnings("SameParameterValue")
    private void add(TranslationBuilder gen, Descriptive entry, String child, String english) {
        gen.add(entry.getDescriptionId(child), english);
    }
}
