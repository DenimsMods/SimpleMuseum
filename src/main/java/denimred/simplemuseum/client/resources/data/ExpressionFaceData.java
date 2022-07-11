package denimred.simplemuseum.client.resources.data;

import com.google.common.collect.Sets;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ExpressionFaceData {
    public final int index;

    private final String bone;
    private final int cube;
    private final Direction face;
    private final float[] newPos;

    public ExpressionFaceData(int index, String bone, int cube, Direction face, float[] newPos) {
        this.index = index;
        this.bone = bone;
        this.cube = cube;
        this.face = face;
        this.newPos = newPos;
    }

    public String getBone() {
        return this.bone;
    }
    public int getCube() {
        return this.cube;
    }
    public Direction getFace() {
        return this.face;
    }
    public float[] getNewPosition() {
        return this.newPos;
    }

    public int getIndex() {
        return this.index;
    }
}
