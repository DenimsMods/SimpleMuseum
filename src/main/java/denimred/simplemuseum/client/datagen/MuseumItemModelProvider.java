package denimred.simplemuseum.client.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Objects;
import java.util.function.Supplier;

import denimred.simplemuseum.common.init.MuseumItems;

public class MuseumItemModelProvider extends ItemModelProvider {
    public MuseumItemModelProvider(
            DataGenerator generator, String modId, ExistingFileHelper existingFileHelper) {
        super(generator, modId, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        this.handheld(MuseumItems.CURATORS_CANE);
        this.handheld(MuseumItems.MOVEMENT_MALLET);
    }

    @SuppressWarnings("SameParameterValue")
    protected void handheld(Supplier<? extends Item> item) {
        final ResourceLocation name = Objects.requireNonNull(item.get().getRegistryName());
        final ResourceLocation tex =
                new ResourceLocation(name.getNamespace(), ITEM_FOLDER + "/" + name.getPath());
        this.withExistingParent(name.toString(), "item/handheld").texture("layer0", tex);
    }
}
