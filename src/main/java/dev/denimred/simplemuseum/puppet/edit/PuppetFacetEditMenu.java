package dev.denimred.simplemuseum.puppet.edit;

import dev.denimred.simplemuseum.puppet.PuppetContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class PuppetFacetEditMenu extends AbstractContainerMenu {
    public final PuppetContext ctx;

    public PuppetFacetEditMenu(int containerId, PuppetContext ctx) {
        super(null, containerId);
        this.ctx = ctx;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.canUseGameMasterBlocks() && ctx.isValid();
    }
}
