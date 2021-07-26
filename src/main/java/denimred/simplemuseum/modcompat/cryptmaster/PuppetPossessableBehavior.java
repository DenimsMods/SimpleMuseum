package denimred.simplemuseum.modcompat.cryptmaster;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

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
            old.recalculateSize();
        }
    }

    @Override
    public void applyActing(PlayerEntity player) {
        if (puppet.getPossessor() != player) {
            puppet.setPossessor(player);
            player.recalculateSize();
        }
        // While the puppet is dying, don't move it (makes it appear more seamless)
        if (!puppet.isDead() || puppet.isCompletelyDead()) {
            puppet.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
            puppet.setMotion(player.getMotion());
            puppet.setNoGravity(true);
            puppet.setSneaking(player.isSneaking());
            puppet.setSprinting(player.isSprinting());
            puppet.setSwimming(player.isSwimming());
            puppet.setPose(player.getPose());

            puppet.rotationYaw = player.rotationYaw;
            puppet.prevRotationYaw = player.prevRotationYaw;
            puppet.rotationPitch = player.rotationPitch;
            puppet.prevRotationPitch = player.prevRotationPitch;
            puppet.rotationYawHead = player.rotationYawHead;
            puppet.prevRotationYawHead = player.prevRotationYawHead;
            puppet.renderYawOffset = player.renderYawOffset;
            puppet.prevRenderYawOffset = player.prevRenderYawOffset;
        }
    }
}
