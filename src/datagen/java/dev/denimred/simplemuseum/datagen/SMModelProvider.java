package dev.denimred.simplemuseum.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;

import static dev.denimred.simplemuseum.init.SMItems.CURATORS_CANE;
import static net.minecraft.data.models.model.ModelTemplates.FLAT_HANDHELD_ITEM;

class SMModelProvider extends FabricModelProvider {
    SMModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateItemModels(ItemModelGenerators gen) {
        gen.generateFlatItem(CURATORS_CANE, FLAT_HANDHELD_ITEM);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators gen) {}
}
