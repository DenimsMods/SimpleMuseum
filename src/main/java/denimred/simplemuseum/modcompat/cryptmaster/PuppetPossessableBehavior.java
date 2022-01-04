package denimred.simplemuseum.modcompat.cryptmaster;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import cryptcraft.cryptcomp.entity.EntityComponent;
import cryptcraft.cryptmaster.IPossessableBehavior;
import cryptcraft.cryptmaster.PossessableComponent;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;

public class PuppetPossessableBehavior implements IPossessableBehavior {
    private final PuppetEntity puppet;

    public PuppetPossessableBehavior(PuppetEntity puppet) {
        this.puppet = puppet;
    }

    public static void register() {
        EntityComponent.INSTANCE.registerInitializer(
                PuppetEntity.class,
                PossessableComponent.class,
                entity -> PuppetPossessableBehavior.createComponent((PuppetEntity) entity));
    }

    public static PossessableComponent createComponent(PuppetEntity puppet) {
        final PossessableComponent comp = new PossessableComponent(puppet);
        comp.setBehavior(new PuppetPossessableBehavior(puppet));
        return comp;
    }

    @Override
    public void startPossess() {}

    @Override
    public void endPossess() {
        final Entity old = puppet.getPossessor();
        puppet.setPossessor(null);
        if (old != null) {
            old.refreshDimensions();
        }
    }

    @Override
    public void applyActing(Player player) {
        if (puppet.getPossessor() != player) {
            puppet.setPossessor(player);
            player.refreshDimensions();
        }
        // While the puppet is dying, don't move it (makes it appear more seamless)
        if (!puppet.isDead() || puppet.isCompletelyDead()) {
            puppet.setPos(player.getX(), player.getY(), player.getZ());
            puppet.setDeltaMovement(player.getDeltaMovement());
            puppet.setNoGravity(true);
            puppet.setShiftKeyDown(player.isShiftKeyDown());
            puppet.setSprinting(player.isSprinting());
            puppet.setSwimming(player.isSwimming());
            puppet.setPose(player.getPose());

            puppet.yRot = player.yRot;
            puppet.yRotO = player.yRotO;
            puppet.xRot = player.xRot;
            puppet.xRotO = player.xRotO;
            puppet.yHeadRot = player.yHeadRot;
            puppet.yHeadRotO = player.yHeadRotO;
            puppet.yBodyRot = player.yBodyRot;
            puppet.yBodyRotO = player.yBodyRotO;
        }
    }
}
