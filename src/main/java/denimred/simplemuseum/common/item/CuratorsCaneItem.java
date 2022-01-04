package denimred.simplemuseum.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SimpleFoiledItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;

public class CuratorsCaneItem extends SimpleFoiledItem {
    public CuratorsCaneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof PuppetEntity) {
            if (!player.level.isClientSide) {
                return InteractionResult.CONSUME;
            } else {
                ClientUtil.openPuppetScreen((PuppetEntity) target, null);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final Level world = context.getLevel();
        if (!(world instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        } else {
            final Vec3 pos = context.getClickLocation();
            final Player player = context.getPlayer();
            PuppetEntity.spawn((ServerLevel) world, pos, player);

            return InteractionResult.CONSUME;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level world, Player player, InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!(world instanceof ServerLevel)) {
            final PuppetEntity puppet = ClientUtil.getSelectedPuppet();
            if (puppet != null) {
                ClientUtil.openPuppetScreen(puppet, null);
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level worldIn, BlockPos pos, Player player) {
        return !player.isCreative();
    }
}
