package denimred.simplemuseum.common.entity.puppet.goals.movement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;

public class MovementData extends SavedData {
    public static HashMap<String, Movement> DATA = new HashMap<>();

    public MovementData() {
        super("sm_movements");
    }

    @Override
    public void load(CompoundTag tag) {
        ListTag movements = tag.getList("movements", Constants.NBT.TAG_COMPOUND);
        movements.forEach(m -> {
            CompoundTag movementTag = (CompoundTag) m;
            Movement movement;
            switch (movementTag.getInt("movementType")) {
                case Movement.Path.MOVEMENT_TYPE:
                    movement = new Movement.Path();
                    break;
                case Movement.Area.MOVEMENT_TYPE:
                    movement = new Movement.Area();
                    break;
                default:
                    movement = null;
            }
            if(movement != null)
                movement.deserializeNBT(movementTag);
        });
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag movements = new ListTag();
        DATA.forEach((id, movement) -> {
            CompoundTag movementTag = new CompoundTag();
            movementTag.putString("id", id);
            movementTag.put("data", movement.serializeNBT());
            movements.add(movementTag);
        });
        tag.put("movements", movements);
        return tag;
    }
}
