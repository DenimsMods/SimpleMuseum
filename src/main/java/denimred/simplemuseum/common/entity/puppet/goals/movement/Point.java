package denimred.simplemuseum.common.entity.puppet.goals.movement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class Point {
    public Vec3 pos;
    public String animation;
    public int minTicks, maxTicks;
    public OnComplete onComplete = OnComplete.Next_Sibling;
    public List<Point> childPoints = new ArrayList<>();

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        //Position
        ListTag posTag = new ListTag();
        posTag.add(DoubleTag.valueOf(pos.x));
        posTag.add(DoubleTag.valueOf(pos.y));
        posTag.add(DoubleTag.valueOf(pos.z));
        tag.put("pos", posTag);

        //Animation
        if(!animation.isEmpty())
            tag.putString("animation", animation);

        //Min/Max
        tag.putIntArray("minMax", new int[] {minTicks, maxTicks});

        //OnComplete
        tag.putInt("onComplete", onComplete.ordinal());

        //Children
        if(!childPoints.isEmpty()) {
            ListTag childTags = new ListTag();
            for (Point child : childPoints)
                childTags.add(child.serializeNBT());
            tag.put("children", childTags);
        }

        return tag;
    }

    public static Point deserializeNBT(CompoundTag tag) {
        Point point = new Point();

        //Position
        ListTag posTag = tag.getList("pos", Constants.NBT.TAG_DOUBLE);
        if(posTag.size() != 3) //ToDo Handle incorrect NBT
            return null;
        point.pos = new Vec3(posTag.getDouble(0), posTag.getDouble(1), posTag.getDouble(2));

        //Animation
        if(tag.contains("animation"))
            point.animation = tag.getString("animation");

        //Min/Max
        if(tag.contains("minMax")) {
            int[] minMax = tag.getIntArray("minMax");
            point.minTicks = minMax[0];
            point.maxTicks = minMax[1];
        }

        //OnComplete
        point.onComplete = OnComplete.values()[tag.getInt("onComplete")];

        //Children
        if(tag.contains("children")) {
            ListTag childTags = tag.getList("children", Constants.NBT.TAG_COMPOUND);
            for(Tag t : childTags)
                point.childPoints.add(deserializeNBT((CompoundTag) t));
        }

        return point;
    }

    public enum OnComplete {
        Return_To_Parent, Next_Sibling, Random_Sibling, Completely_Random
    }
}