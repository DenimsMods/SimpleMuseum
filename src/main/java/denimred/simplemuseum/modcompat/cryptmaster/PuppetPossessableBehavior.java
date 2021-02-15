package denimred.simplemuseum.modcompat.cryptmaster;

import net.minecraft.entity.player.PlayerEntity;

import cryptcraft.cryptmaster.IPossessableBehavior;
import cryptcraft.cryptmaster.PossessableComponent;
import denimred.simplemuseum.common.entity.MuseumPuppetEntity;

public class PuppetPossessableBehavior implements IPossessableBehavior {
    private final MuseumPuppetEntity puppet;

    public PuppetPossessableBehavior(MuseumPuppetEntity puppet) {
        this.puppet = puppet;
    }

    public static PossessableComponent createComponent(MuseumPuppetEntity puppet) {
        final PossessableComponent comp = new PossessableComponent(puppet);
        comp.setBehavior(new PuppetPossessableBehavior(puppet));
        return comp;
    }

    @Override
    public void startPossess() {}

    @Override
    public void endPossess() {}

    @Override
    public void applyActing(PlayerEntity player) {
        puppet.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
        puppet.setMotion(player.getMotion());
        puppet.setNoGravity(true);
        puppet.setSneaking(player.isSneaking());
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
