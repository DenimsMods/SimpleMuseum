package denimred.simplemuseum.client.resources.data;

import com.google.common.collect.Sets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ExpressionData {
    public final int index;

    public final String name;
    public final boolean interpolated;
    public final List<ExpressionFaceData> areas;
    public final boolean enabled;

    public ExpressionData(int index, String name, boolean interpolated, List<ExpressionFaceData> areas) {
        this.index = index;
        this.name = name;
        this.interpolated = interpolated;
        this.areas = areas;
        this.enabled = !name.equals("");
    }

    private ExpressionFaceData getArea(int id) {
        return this.areas.get(id);
    }

    public int getAreaIndex(int id) {
        return this.areas.get(id).getCube();
    }

    public int getIndex() {
        return this.index;
    }

    public Set<Integer> getUniqueAreaIndices() {
        Set<Integer> set = Sets.newHashSet();

        for(ExpressionFaceData area : this.areas) {
            set.add(area.getCube());
        }

        return set;
    }
}
