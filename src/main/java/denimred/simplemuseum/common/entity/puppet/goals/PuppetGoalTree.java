package denimred.simplemuseum.common.entity.puppet.goals;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.List;

public class PuppetGoalTree {

    private final List<PuppetGoal> goalList = new ArrayList<>();

    public List<PuppetGoal> getGoalList() {
        return goalList;
    }

    public PuppetGoal[] getGoalArray() {
        return goalList.toArray(new PuppetGoal[]{});
    }

//    private final List<PuppetGoal> goalList = new ArrayList<>();
//
//    public List<PuppetGoal> getGoalList() {
//        return goalList;
//    }
//
//    public PuppetGoal[] getGoalArray() {
//        return goalList.toArray(new PuppetGoal[]{});
//    }

}
