package denimred.simplemuseum.client;

import denimred.simplemuseum.common.entity.puppet.goals.movement.Movement;

public class MovementEditorClient {

    private static Movement currentMovement = null;

    public static boolean isEditing() {
        return currentMovement != null;
    }

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

    public static Movement getCurrentMovement() {
        return currentMovement;
    }

}
