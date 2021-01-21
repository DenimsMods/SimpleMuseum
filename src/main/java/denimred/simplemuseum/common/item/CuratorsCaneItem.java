package denimred.simplemuseum.common.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.SimpleFoiledItem;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Objects;

import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.entity.MuseumDummyEntity;
import denimred.simplemuseum.common.init.MuseumEntities;

public class CuratorsCaneItem extends SimpleFoiledItem {
    public CuratorsCaneItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType itemInteractionForEntity(
            ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand) {
        if (target instanceof MuseumDummyEntity) {
            if (!player.world.isRemote) {
                return ActionResultType.CONSUME;
            } else {
                ClientUtil.openDummyScreen((MuseumDummyEntity) target);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.FAIL;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        final World world = context.getWorld();
        if (!(world instanceof ServerWorld)) {
            return ActionResultType.SUCCESS;
        } else {
            final ItemStack stack = context.getItem();
            final BlockPos pos = context.getPos();
            final Direction direction = context.getFace();
            final BlockState state = world.getBlockState(pos);

            final BlockPos offsetPos;
            if (state.getCollisionShape(world, pos).isEmpty()) {
                offsetPos = pos;
            } else {
                offsetPos = pos.offset(direction);
            }

            final PlayerEntity player = context.getPlayer();
            MuseumEntities.MUSEUM_DUMMY
                    .get()
                    .spawn(
                            (ServerWorld) world,
                            stack,
                            player,
                            offsetPos,
                            SpawnReason.SPAWN_EGG,
                            true,
                            !Objects.equals(pos, offsetPos) && direction == Direction.UP);

            return ActionResultType.CONSUME;
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity player, Hand hand) {
        final ItemStack stack = player.getHeldItem(hand);
        final BlockRayTraceResult rayTrace =
                rayTrace(worldIn, player, RayTraceContext.FluidMode.SOURCE_ONLY);
        if (rayTrace.getType() != RayTraceResult.Type.BLOCK) {
            return ActionResult.resultPass(stack);
        } else if (!(worldIn instanceof ServerWorld)) {
            return ActionResult.resultSuccess(stack);
        } else {
            final BlockPos blockPos = rayTrace.getPos();
            if (!(worldIn.getBlockState(blockPos).getBlock() instanceof FlowingFluidBlock)) {
                return ActionResult.resultPass(stack);
            } else if (worldIn.isBlockModifiable(player, blockPos)
                    && player.canPlayerEdit(blockPos, rayTrace.getFace(), stack)) {
                final Entity dummy =
                        MuseumEntities.MUSEUM_DUMMY
                                .get()
                                .spawn(
                                        (ServerWorld) worldIn,
                                        stack,
                                        player,
                                        blockPos,
                                        SpawnReason.SPAWN_EGG,
                                        false,
                                        false);
                if (dummy != null) {
                    player.addStat(Stats.ITEM_USED.get(this));
                    return ActionResult.resultConsume(stack);
                } else {
                    return ActionResult.resultPass(stack);
                }
            } else {
                return ActionResult.resultFail(stack);
            }
        }
    }

    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target instanceof MuseumDummyEntity) {
            target.remove();
            return true;
        }
        return false;
    }
}
