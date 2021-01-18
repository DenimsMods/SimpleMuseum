package denimred.simplemuseum.client.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.item.SpawnEggItem;
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
        this.spawnEgg(MuseumItems.MUSEUM_DUMMY_SPAWN_EGG);
    }

    protected void spawnEgg(Supplier<? extends SpawnEggItem> egg) {
        final String name = Objects.requireNonNull(egg.get().getRegistryName()).getPath();
        this.withExistingParent(name, "item/template_spawn_egg");
    }
}
