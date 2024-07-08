package dev.denimred.simplemuseum.cane;

import dev.denimred.simplemuseum.init.SMEntityTypes;
import dev.denimred.simplemuseum.puppet.entity.Puppet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CuratorsCaneItem extends Item {
    public static final Component ILLEGAL_USAGE = Component.translatable("advMode.notAllowed").withStyle(ChatFormatting.RED);
    protected final EntityType<? extends Puppet> type;

    public CuratorsCaneItem() {
        this(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON), SMEntityTypes.PUPPET);
    }

    public CuratorsCaneItem(Properties properties, EntityType<? extends Puppet> type) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null || !player.canUseGameMasterBlocks()) {
            sendFailMessage(player);
            return InteractionResult.FAIL;
        }

        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;

        var stack = context.getItemInHand();
        var blockPos = context.getClickedPos();
        var direction = context.getClickedFace();
        var state = level.getBlockState(blockPos);

        BlockPos blockPos2;
        if (state.getCollisionShape(level, blockPos).isEmpty()) {
            blockPos2 = blockPos;
        } else {
            blockPos2 = blockPos.relative(direction);
        }

        var shouldOffsetYMore = !Objects.equals(blockPos, blockPos2) && direction == Direction.UP;
        var puppet = type.spawn(level, stack, context.getPlayer(), blockPos2, MobSpawnType.SPAWN_EGG, true, shouldOffsetYMore);
        if (puppet != null) {
            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Puppet puppet)) return InteractionResult.PASS;
        if (!player.canUseGameMasterBlocks()) {
            sendFailMessage(player);
            return InteractionResult.FAIL;
        }
        player.openMenu(puppet);
        return InteractionResult.sidedSuccess(player.getCommandSenderWorld().isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    private void sendFailMessage(@Nullable Player player) {
        if (!(player instanceof ServerPlayer sp)) return;
        sp.sendSystemMessage(ILLEGAL_USAGE, true);
    }
}
