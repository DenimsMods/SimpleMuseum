package dev.denimred.simplemuseum.puppet;

import dev.denimred.simplemuseum.puppet.data.PuppetFacetStore;
import dev.denimred.simplemuseum.puppet.edit.PuppetFacetEditMenu;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface PuppetContext extends MenuProvider {
    PuppetFacetStore facets();

    boolean isValid();

    @Override
    default @Nullable PuppetFacetEditMenu createMenu(int counter, Inventory inventory, Player player) {
        if (!player.canUseGameMasterBlocks() || !isValid()) return null;
        return new PuppetFacetEditMenu(counter, this);
    }

    FabricPacket createOpenMenuPacket(PuppetFacetEditMenu menu);
}
