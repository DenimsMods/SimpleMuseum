package denimred.simplemuseum.common.init;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.item.CuratorsCaneItem;

public class MuseumItems {
    public static final DeferredRegister<Item> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ITEMS, SimpleMuseum.MOD_ID);

    public static final RegistryObject<CuratorsCaneItem> CURATORS_CANE =
            REGISTRY.register(
                    "curators_cane",
                    () ->
                            new CuratorsCaneItem(
                                    new Item.Properties()
                                            .maxStackSize(1)
                                            .rarity(Rarity.UNCOMMON)
                                            .group(ItemGroup.MISC)));
}
