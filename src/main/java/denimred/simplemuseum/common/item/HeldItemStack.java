package denimred.simplemuseum.common.item;

import net.minecraft.world.item.ItemStack;

public class HeldItemStack {

    public ItemStack itemStack;
    public boolean armorDisplay = false;
    public float scale = 1.0f;

    public HeldItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

}
