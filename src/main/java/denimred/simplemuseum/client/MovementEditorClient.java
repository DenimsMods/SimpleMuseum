package denimred.simplemuseum.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

import denimred.simplemuseum.common.entity.puppet.goals.movement.Movement;

public class MovementEditorClient {

    private static Movement currentMovement = null;
    private static EditMode editMode = EditMode.MOVE;

    public static Movement createNewMovement(Movement.MoveType moveType) {
        switch (moveType) {
            case Area:
                currentMovement = new Movement.Area();
                break;
            case Path:
                currentMovement = new Movement.Path();
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
        MOVE(ChatFormatting.RED),
        POI(ChatFormatting.AQUA);

        ChatFormatting formatting;
        EditMode(ChatFormatting formatting) {
            this.formatting = formatting;
        }
    }

}
