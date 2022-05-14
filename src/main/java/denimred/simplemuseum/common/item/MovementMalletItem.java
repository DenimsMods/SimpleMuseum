package denimred.simplemuseum.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SimpleFoiledItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import denimred.simplemuseum.client.MovementEditorClient;
import denimred.simplemuseum.client.gui.screen.behavior.PuppetMovementEditorScreen;
import denimred.simplemuseum.client.gui.screen.behavior.PuppetMovementSelectScreen;
import denimred.simplemuseum.common.entity.puppet.goals.movement.Point;

//Alternatively; MovementMilk :)
public class MovementMalletItem extends SimpleFoiledItem {
    public MovementMalletItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final Level world = context.getLevel();
        if ((world instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        } else {
            final Vec3 pos = context.getClickLocation();
            if(!MovementEditorClient.isEditing())
                Minecraft.getInstance().setScreen(new PuppetMovementSelectScreen(null, null));
            else if (context.getPlayer().isShiftKeyDown())
                Minecraft.getInstance().setScreen(new PuppetMovementEditorScreen(MovementEditorClient.getCurrentMovement()));
            else {
                Point newPoint = new Point();
                newPoint.pos = new Vec3(Math.round(pos.x * 100.0) / 100.0, Math.round(pos.y * 100.0) / 100.0, Math.round(pos.z * 100.0) / 100.0);
                MovementEditorClient.getCurrentMovement().addPoint(newPoint);
                context.getPlayer().displayClientMessage(new TextComponent("Added pos: " + newPoint.pos).withStyle(ChatFormatting.AQUA), true);
            }
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }
}
