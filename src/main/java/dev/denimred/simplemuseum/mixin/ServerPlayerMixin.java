package dev.denimred.simplemuseum.mixin;

import com.mojang.authlib.GameProfile;
import dev.denimred.simplemuseum.cane.CuratorsCaneItem;
import dev.denimred.simplemuseum.puppet.PuppetContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    @Shadow
    private int containerCounter;

    private ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Shadow
    protected abstract void nextContainerCounter();

    @Shadow
    protected abstract void initMenu(AbstractContainerMenu menu);

    @Inject(method = "openMenu", at = @At("HEAD"), cancellable = true)
    private void handlePuppetFacetEditMenu(MenuProvider provider, CallbackInfoReturnable<OptionalInt> cir) {
        if (!(provider instanceof PuppetContext ctx)) return;
        if (containerMenu != inventoryMenu) closeContainer();
        nextContainerCounter();
        var menu = ctx.createMenu(containerCounter, getInventory(), this);
        if (menu == null) {
            displayClientMessage(CuratorsCaneItem.ILLEGAL_USAGE, true);
            cir.setReturnValue(OptionalInt.empty());
        } else {
            ServerPlayNetworking.send((ServerPlayer) (Object) this, ctx.createOpenMenuPacket(menu));
            initMenu(menu);
            containerMenu = menu;
            cir.setReturnValue(OptionalInt.of(menu.containerId));
        }
    }
}
