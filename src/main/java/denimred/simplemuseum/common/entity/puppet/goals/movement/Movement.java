package denimred.simplemuseum.common.entity.puppet.goals.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import denimred.simplemuseum.client.event.AreaHandler;

public abstract class Movement implements INBTSerializable<CompoundTag> {

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("movementType", getMoveType().ordinal());
        return serializeNBT(tag);
    }

    public CompoundTag serializeNBT(CompoundTag tag) {
        return tag;
    }

    public abstract MoveType getMoveType();

    /**
     *Point-to-Point movement.
     * Let Minecraft AI fill the inbetweens.
     */
    public static class Path extends Movement {
        public static final MoveType moveType = MoveType.Area;
        private final LinkedList<Point> movementPoints = new LinkedList<>();
        private LoopType loopType;

        @Override
        public CompoundTag serializeNBT(CompoundTag tag) {
            ListTag posList = new ListTag();
            movementPoints.forEach(point -> posList.add(point.serializeNBT()));
            tag.put("points", posList);
            tag.putInt("loopType", loopType.ordinal());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            ListTag posList = tag.getList("points", Constants.NBT.TAG_COMPOUND);
            posList.forEach(pointTag -> addPoint(Point.deserializeNBT((CompoundTag) pointTag)));
            setLoopType(LoopType.values()[tag.getInt("loopType")]);
        }

        public void addPoint(Point pos) {
            movementPoints.add(pos);
        }

        public void setLoopType(LoopType loopType) {
            this.loopType = loopType;
        }

        public List<Point> getMovementPoints() {
            return movementPoints;
        }

        public LoopType getLoopType() {
            return loopType;
        }

        public MoveType getMoveType() {
            return moveType;
        }
    }

    /**
     * Area-defined wandering.
     * If no area is defined, but there are PoIs defined, the puppet will instead choose between these to wander between.
     */
    public static class Area extends Movement {
        public static final MoveType moveType = MoveType.Area;
        private AABB movementArea;
        private BlockPos pos1, pos2;
        private List<Point> pointsOfInterest = new ArrayList<>();

        @Override
        public CompoundTag serializeNBT(CompoundTag tag) {
            ListTag poiList = new ListTag();
            pointsOfInterest.forEach(point -> poiList.add(point.serializeNBT()));
            tag.put("pois", poiList);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            ListTag poiList = tag.getList("pois", Constants.NBT.TAG_COMPOUND);
            poiList.forEach(poiTag -> addPOI(Point.deserializeNBT((CompoundTag) poiTag)));
        }

        public void addPOI(Point point) {
            pointsOfInterest.add(point);
        }

        public void setPos1(BlockPos pos) {
            pos1 = pos;
        }

        public void setPos2(BlockPos pos) {
            pos2 = pos;
        }

        public void setMovementArea() {
            movementArea = new AABB(pos1, pos2);
//            movementArea.expandTowards(1, 1, 1);
            AreaHandler.area = movementArea;
        }

        public AABB getMovementArea() {
            return movementArea;
        }

        public BlockPos getPos1() {
            return pos1;
        }

        public BlockPos getPos2() {
            return pos2;
        }

        public List<Point> getPointsOfInterest() {
            return pointsOfInterest;
        }

        public boolean isComplete() {
            return pos1 != null && pos2 != null;
        }

        public MoveType getMoveType() {
            return moveType;
        }
    }

    public enum MoveType { Path, Area }
    public enum LoopType { NO_LOOP, LOOP, FWD_AND_BCK }

}