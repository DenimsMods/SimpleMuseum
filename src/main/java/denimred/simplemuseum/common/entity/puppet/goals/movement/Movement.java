package denimred.simplemuseum.common.entity.puppet.goals.movement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public abstract class Movement implements INBTSerializable<CompoundTag> {

    private final List<Point> movementPoints = new ArrayList<>();

    public void addPoint(Point pos) {
        movementPoints.add(pos);
    }

    public List<Point> getMovementPoints() {
        return movementPoints;
    }

    public CompoundTag serializeNBT() {
        return serializeNBT(new CompoundTag());
    }

    public CompoundTag serializeNBT(CompoundTag tag) {
        ListTag posList = new ListTag();
        movementPoints.forEach(point -> posList.add(point.serializeNBT()));
        tag.put("points", posList);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        ListTag posList = tag.getList("points", Constants.NBT.TAG_COMPOUND);
        posList.forEach(pointTag -> addPoint(Point.deserializeNBT((CompoundTag) pointTag)));
    }

    public static class Path extends Movement {
        public static final int MOVEMENT_TYPE = 0;
        private LoopType loopType;

        public void setLoopType(LoopType loopType) {
            this.loopType = loopType;
        }

        @Override
        public CompoundTag serializeNBT(CompoundTag tag) {
            super.serializeNBT(tag);
            tag.putInt("movementType", MOVEMENT_TYPE);
            tag.putInt("loopType", loopType.ordinal());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            super.deserializeNBT(tag);
            setLoopType(LoopType.values()[tag.getInt("loopType")]);
        }

        public LoopType getLoopType() {
            return loopType;
        }
    }

    public static class Area extends Movement {
        public static final int MOVEMENT_TYPE = 1;

        @Override
        public CompoundTag serializeNBT(CompoundTag tag) {
            super.serializeNBT(tag);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            super.deserializeNBT(tag);
        }
    }

    public enum MoveType { Path, Area }
    public enum LoopType { NO_LOOP, LOOP, FWD_AND_BCK }

}
