package denimred.simplemuseum.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SimpleFoiledItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import denimred.simplemuseum.client.MovementEditorClient;
import denimred.simplemuseum.client.event.AreaHandler;
import denimred.simplemuseum.client.gui.screen.behavior.PuppetMovementEditorScreen;
import denimred.simplemuseum.client.gui.screen.behavior.PuppetMovementSelectScreen;
import denimred.simplemuseum.common.entity.puppet.goals.movement.Movement;
import denimred.simplemuseum.common.entity.puppet.goals.movement.Point;

//Alternatively; MovementMilk :)
//This is a temp item. Unsure on the plan, current proposed idea is to have multiple "modes" for the Cane instead.
public class MovementMalletItem extends SimpleFoiledItem {
    public MovementMalletItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide()) {
            if(!MovementEditorClient.isEditing())
                Minecraft.getInstance().setScreen(new PuppetMovementSelectScreen(null, null));
            else {
                Movement movement = MovementEditorClient.getCurrentMovement();
                if (player.isShiftKeyDown()) {
                    int i = MovementEditorClient.getEditMode().ordinal() + 1;
//                MovementEditorClient.setEditMode(MovementEditorClient.EditMode.values()[i >= MovementEditorClient.EditMode.values().length ? 0 : i]);
                    Minecraft.getInstance().setScreen(new PuppetMovementEditorScreen(MovementEditorClient.getCurrentMovement()));
                }
                else if (movement.getMoveType() == Movement.MoveType.Area && ((Movement.Area)movement).isComplete())
                    AreaHandler.lock();
            }
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final Level world = context.getLevel();
        if ((world instanceof ServerLevel))
            return InteractionResult.SUCCESS;

        if(!MovementEditorClient.isEditing() || context.getPlayer().isShiftKeyDown()) {
            use(context.getLevel(), context.getPlayer(), context.getHand());
            return InteractionResult.SUCCESS;
        }

        if(MovementEditorClient.isEditing()) {
            Movement movement = MovementEditorClient.getCurrentMovement();
            Vec3 clickPos = context.getClickLocation();
            final Vec3 pos = new Vec3(Math.round(clickPos.x * 100.0) / 100.0, Math.round(clickPos.y * 100.0) / 100.0, Math.round(clickPos.z * 100.0) / 100.0);

            Point newPoint;
            switch (MovementEditorClient.getEditMode()) {
                case PATH:
                    newPoint = new Point();
                    newPoint.pos = pos;
                    ((Movement.Path) movement).addPoint(newPoint);
                    break;
                case AREA:
                    Movement.Area areaMovement = (Movement.Area)movement;
                    if(areaMovement.getPos1() == null)
                        areaMovement.setPos1(context.getClickedPos());
                    else
                        areaMovement.setPos2(context.getClickedPos());
                    if(areaMovement.isComplete())
                        areaMovement.setMovementArea();
                    break;
                case POI:
                    newPoint = new Point();
                    newPoint.pos = pos;
                    ((Movement.Area) movement).addPOI(newPoint);
                    break;
                default:
                    break;
            }

            context.getPlayer().displayClientMessage(new TextComponent("Added pos: " + pos).withStyle(ChatFormatting.AQUA), true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }
}
