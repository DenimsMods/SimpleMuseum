package denimred.simplemuseum.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
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
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.goals.movement.Movement;

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
            if(!MovementEditorClient.isEditing()) {
                MovementEditorClient.createNewMovement(Movement.MoveType.Path);
                context.getPlayer().displayClientMessage(new TextComponent("Started creating new Movement").withStyle(ChatFormatting.GREEN), true);
            }
            else {
                MovementEditorClient.getCurrentMovement().addPos(context.getClickLocation());
                context.getPlayer().displayClientMessage(new TextComponent("Added pos: " + context.getClickLocation()).withStyle(ChatFormatting.AQUA), true);
            }
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }
}
