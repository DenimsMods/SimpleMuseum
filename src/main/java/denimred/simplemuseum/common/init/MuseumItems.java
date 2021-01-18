package denimred.simplemuseum.common.init;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.item.DeferredSpawnEgg;

public class MuseumItems {
    public static final DeferredRegister<Item> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ITEMS, SimpleMuseum.MOD_ID);

    public static final RegistryObject<DeferredSpawnEgg> MUSEUM_DUMMY_SPAWN_EGG =
            REGISTRY.register(
                    "museum_dummy_spawn_egg",
                    () ->
                            new DeferredSpawnEgg(
                                    MuseumEntities.MUSEUM_DUMMY,
                                    0xF1F1F1,
                                    0x6696CB,
                                    new Item.Properties().group(ItemGroup.MISC)));
}
