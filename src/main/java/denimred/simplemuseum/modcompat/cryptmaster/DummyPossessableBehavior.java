package denimred.simplemuseum.modcompat.cryptmaster;

import net.minecraft.entity.player.PlayerEntity;

import cryptcraft.cryptmaster.IPossessableBehavior;
import cryptcraft.cryptmaster.PossessableComponent;
import denimred.simplemuseum.common.entity.MuseumDummyEntity;

public class DummyPossessableBehavior implements IPossessableBehavior {
    private final MuseumDummyEntity dummy;

    public DummyPossessableBehavior(MuseumDummyEntity dummy) {
        this.dummy = dummy;
    }

    public static PossessableComponent createComponent(MuseumDummyEntity dummy) {
        final PossessableComponent comp = new PossessableComponent(dummy);
        comp.setBehavior(new DummyPossessableBehavior(dummy));
        return comp;
    }

    @Override
    public void startPossess() {}

    @Override
    public void endPossess() {}

    @Override
    public void applyActing(PlayerEntity player) {
        dummy.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
        dummy.setMotion(player.getMotion());
        dummy.setNoGravity(true);
        dummy.setSneaking(player.isSneaking());
        dummy.setSwimming(player.isSwimming());
        dummy.setPose(player.getPose());

        dummy.rotationYaw = player.rotationYaw;
        dummy.prevRotationYaw = player.prevRotationYaw;
        dummy.rotationPitch = player.rotationPitch;
        dummy.prevRotationPitch = player.prevRotationPitch;
        dummy.rotationYawHead = player.rotationYawHead;
        dummy.prevRotationYawHead = player.prevRotationYawHead;
        dummy.renderYawOffset = player.renderYawOffset;
        dummy.prevRenderYawOffset = player.prevRenderYawOffset;
    }
}
