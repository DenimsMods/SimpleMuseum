package denimred.simplemuseum.common.entity.puppet.manager.value.primitive;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

public final class IntValue extends PuppetValue<Integer, IntProvider> {
    protected IntValue(IntProvider provider, PuppetValueManager manager) {
        super(provider, manager);
    }

    @Override
    public void set(Integer value) {
        if (this.test(value)) {
            super.set(value);
        } else {
            super.set(value < provider.min ? provider.min : provider.max);
        }
    }

    @Override
    public boolean test(Integer value) {
        return value >= provider.min && value <= provider.max;
    }
}
