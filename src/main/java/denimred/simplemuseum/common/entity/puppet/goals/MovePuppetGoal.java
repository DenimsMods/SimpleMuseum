package denimred.simplemuseum.common.entity.puppet.goals;

import java.util.EnumSet;

import denimred.simplemuseum.common.entity.puppet.goals.movement.Movement;

public class MovePuppetGoal extends PuppetGoal {

    private Movement movement;

    public MovePuppetGoal() {
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    public Movement getMovement() {
        return movement;
    }

    public void setMovement(Movement movement) {
        this.movement = movement;
    }

    @Override
    public boolean canUse() {
        return false;
    }

}
