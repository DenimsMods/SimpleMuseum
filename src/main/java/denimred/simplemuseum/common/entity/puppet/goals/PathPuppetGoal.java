package denimred.simplemuseum.common.entity.puppet.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class PathPuppetGoal extends PuppetGoal {

    private final List<Vec3> points = new ArrayList<>();

    public void addPoint(BlockPos blockPos) {
        addPoint(new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }

    public void addPoint(Vec3 newPoint) {
        this.points.add(newPoint);
    }

    public List<Vec3> getPoints() {
        return this.points;
    }

    public Vec3[] getPointsArray() {
        return this.points.toArray(new Vec3[]{});
    }

    @Override
    boolean execute() {
        return false;
    }

    enum LoopType { NO_LOOP, LOOP, FWD_AND_BCK }

}
