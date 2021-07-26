package denimred.simplemuseum.client.util;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Objects;

public class PlayerRotationHelper {
    private static final PlayerEntity PLAYER = Objects.requireNonNull(ClientUtil.MC.player);
    private static float prevRenderYawOffset;
    private static float renderYawOffset;
    private static float prevRotationYaw;
    private static float rotationYaw;
    private static float prevRotationPitch;
    private static float rotationPitch;
    private static float prevRotationYawHead;
    private static float rotationYawHead;

    public static void save() {
        prevRenderYawOffset = PLAYER.prevRenderYawOffset;
        renderYawOffset = PLAYER.renderYawOffset;
        prevRotationYaw = PLAYER.prevRotationYaw;
        rotationYaw = PLAYER.rotationYaw;
        prevRotationPitch = PLAYER.prevRotationPitch;
        rotationPitch = PLAYER.rotationPitch;
        prevRotationYawHead = PLAYER.prevRotationYawHead;
        rotationYawHead = PLAYER.rotationYawHead;
    }

    public static void clear() {
        PLAYER.prevRenderYawOffset = 0.0F;
        PLAYER.renderYawOffset = 0.0F;
        PLAYER.prevRotationYaw = 0.0F;
        PLAYER.rotationYaw = 0.0F;
        PLAYER.prevRotationPitch = 0.0F;
        PLAYER.rotationPitch = 0.0F;
        PLAYER.prevRotationYawHead = 0.0F;
        PLAYER.rotationYawHead = 0.0F;
    }

    public static void load() {
        PLAYER.prevRenderYawOffset = prevRenderYawOffset;
        PLAYER.renderYawOffset = renderYawOffset;
        PLAYER.prevRotationYaw = prevRotationYaw;
        PLAYER.rotationYaw = rotationYaw;
        PLAYER.prevRotationPitch = prevRotationPitch;
        PLAYER.rotationPitch = rotationPitch;
        PLAYER.prevRotationYawHead = prevRotationYawHead;
        PLAYER.rotationYawHead = rotationYawHead;
    }
}
