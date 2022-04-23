package denimred.simplemuseum.common.entity.puppet.manager.value.standard;

import denimred.simplemuseum.common.entity.puppet.goals.PuppetGoalTree;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import denimred.simplemuseum.common.util.ValueSerializers;

public class PuppetGoalTreeProvider extends PuppetValueProvider<PuppetGoalTree, PuppetGoalTreeValue> {

    public PuppetGoalTreeProvider(PuppetKey key) {
        this(key, new PuppetGoalTree());
    }

    protected PuppetGoalTreeProvider(PuppetKey key, PuppetGoalTree path) {
        super(key, path, ValueSerializers.GOAL_TREE, null);
    }

    @Override
    public PuppetGoalTreeValue provideFor(PuppetValueManager manager) {
        return new PuppetGoalTreeValue(this, manager);
    }

}
