package denimred.simplemuseum.common.init;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.item.CuratorsCaneItem;
import net.minecraftforge.registries.RegistryObject;

public final class MuseumItems {
    public static final DeferredRegister<Item> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ITEMS, SimpleMuseum.MOD_ID);

    public static final RegistryObject<CuratorsCaneItem> CURATORS_CANE =
            REGISTRY.register(
                    "curators_cane",
                    () ->
                            new CuratorsCaneItem(
                                    new Item.Properties()
                                            .stacksTo(1)
                                            .rarity(Rarity.UNCOMMON)
                                            .tab(CreativeModeTab.TAB_TOOLS)));
}
