package denimred.simplemuseum.common.entity.puppet.goals.movement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public abstract class Movement implements INBTSerializable<CompoundTag> {

    public static class Path extends Movement {
        public static final int MOVEMENT_TYPE = 0;
        private final List<Vec3> positions = new ArrayList<>();
        private LoopType loopType;

        public List<Vec3> getPositions() {
            return positions;
        }

        public void addPos(Vec3 pos) {
            positions.add(pos);
        }

        public Vec3 removePos(int index) {
            if(index < 0 || index >= positions.size())
                return null;
            return positions.remove(index);
        }

        public boolean removePos(Vec3 pos) {
            if(pos != null)
                return positions.remove(pos);
            return false;
        }

        public void setLoopType(LoopType loopType) {
            this.loopType = loopType;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            ListTag posList = new ListTag();
            positions.forEach(pos -> {
                ListTag posTag = new ListTag();
                posTag.add(DoubleTag.valueOf(pos.x));
                posTag.add(DoubleTag.valueOf(pos.y));
                posTag.add(DoubleTag.valueOf(pos.z));
                posList.add(posTag);
            });
            tag.putInt("movementType", MOVEMENT_TYPE);
            tag.put("positions", posList);
            tag.putInt("loopType", loopType.ordinal());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            ListTag posList = tag.getList("positions", Constants.NBT.TAG_LIST);
            posList.forEach(p -> {
                ListTag posTag = (ListTag)p;
                addPos(new Vec3(posTag.getDouble(0), posTag.getDouble(1), posTag.getDouble(2)));
            });
            setLoopType(LoopType.values()[tag.getInt("loopType")]);
        }

        public LoopType getLoopType() {
            return loopType;
        }
    }
    
    public static class Area extends Movement {
        public static final int MOVEMENT_TYPE = 1;
        @Override
        public CompoundTag serializeNBT() {
            return null;
        }

        @Override
        public void deserializeNBT(CompoundTag arg) {

        }
    }

    enum LoopType { NO_LOOP, LOOP, FWD_AND_BCK }
}
