package denimred.simplemuseum.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;

import denimred.simplemuseum.client.event.AreaHandler;
import denimred.simplemuseum.common.entity.puppet.goals.movement.Movement;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class MovementEditorClient {
    private static Movement currentMovement = null;
    private static EditMode editMode = null;

    public static void tick() {
        if (isEditing() && getEditMode() == EditMode.AREA && ((Movement.Area)currentMovement).isComplete()) {
            AreaHandler.raytrace();
        }
    }

    public static Movement createNewMovement(Movement.MoveType moveType) {
        switch (moveType) {
            case Area:
                currentMovement = new Movement.Area();
                setEditMode(EditMode.AREA);
                break;
            case Path:
                currentMovement = new Movement.Path();
                setEditMode(EditMode.PATH);
                break;
        }
        return currentMovement;
    }

    public static void setEditMode(EditMode mode) {
        editMode = mode;
        Minecraft.getInstance().player.displayClientMessage(new TextComponent("Edit Mode: " + editMode).withStyle(editMode.formatting), true);
    }

    public static Movement getCurrentMovement() {
        return currentMovement;
    }

    public static EditMode getEditMode() {
        return editMode;
    }

    public static boolean isEditing() {
        return currentMovement != null;
    }

    public enum EditMode {
        PATH(ChatFormatting.RED),
        AREA(ChatFormatting.AQUA),
        POI(ChatFormatting.GREEN);

        ChatFormatting formatting;
        EditMode(ChatFormatting formatting) {
            this.formatting = formatting;
        }
    }

}
