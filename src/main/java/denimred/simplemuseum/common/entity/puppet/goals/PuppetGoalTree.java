package denimred.simplemuseum.common.entity.puppet.goals;

import java.util.ArrayList;
import java.util.List;

public class PuppetGoalTree {

    private final List<PuppetGoal> goalList = new ArrayList<>();

    public List<PuppetGoal> GetGoalList() {
        return goalList;
    }

    public PuppetGoal[] GetGoalArray() {
        return goalList.toArray(new PuppetGoal[]{});
    }

}
