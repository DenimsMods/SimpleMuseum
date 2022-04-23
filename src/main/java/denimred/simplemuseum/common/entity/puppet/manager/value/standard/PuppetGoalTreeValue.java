package denimred.simplemuseum.common.entity.puppet.manager.value.standard;

import denimred.simplemuseum.common.entity.puppet.goals.PuppetGoalTree;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

public class PuppetGoalTreeValue extends PuppetValue<PuppetGoalTree, PuppetGoalTreeProvider> {

    protected PuppetGoalTreeValue(PuppetGoalTreeProvider provider, PuppetValueManager manager) {
        super(provider, manager);
    }

}
