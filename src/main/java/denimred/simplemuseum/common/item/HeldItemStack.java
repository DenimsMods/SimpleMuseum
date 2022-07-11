package denimred.simplemuseum.common.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class HeldItemStack {

    public static final String NBT_LIST = "heldItems";
    public static final String NBT_BONENAME = "boneName";
    public static final String NBT_HELDITEM = "heldItem";

    public static final String NBT_ITEMSTACK = "itemStack";
    public static final String NBT_ARMOR = "armorDisplay";
    public static final String NBT_SCALE = "scale";

    public ItemStack itemStack;
    public boolean armorDisplay = false;
    public float scale = 1.0f;

    public HeldItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put(NBT_ITEMSTACK, itemStack.serializeNBT());
        tag.putBoolean(NBT_ARMOR, armorDisplay);
        tag.putFloat(NBT_SCALE, scale);
        return tag;
    }

    public static HeldItemStack deserializeNBT(CompoundTag tag) {
        HeldItemStack heldItemStack = new HeldItemStack(ItemStack.of(tag.getCompound(NBT_ITEMSTACK)));
        heldItemStack.armorDisplay = tag.getBoolean(NBT_ARMOR);
        heldItemStack.scale = tag.getFloat(NBT_SCALE);
        return heldItemStack;
    }

    public void writeToBuf(FriendlyByteBuf buf) {
        buf.writeItemStack(itemStack, false);
        buf.writeBoolean(armorDisplay);
        buf.writeFloat(scale);
    }

    public static HeldItemStack readFromBuf(FriendlyByteBuf buf) {
        ItemStack stack = buf.readItem();

        HeldItemStack heldItem = new HeldItemStack(stack);

        heldItem.armorDisplay = buf.readBoolean();
        heldItem.scale = buf.readFloat();

        return heldItem;
    }

}
