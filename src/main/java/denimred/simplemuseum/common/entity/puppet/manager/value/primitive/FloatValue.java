package denimred.simplemuseum.common.entity.puppet.manager.value.primitive;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

public final class FloatValue extends PuppetValue<Float, FloatProvider> {
    protected FloatValue(FloatProvider provider, PuppetValueManager manager) {
        super(provider, manager);
    }

    @Override
    public void set(Float value) {
        if (this.test(value)) {
            super.set(value);
        } else {
            super.set(value < provider.min ? provider.min : provider.max);
        }
    }

    @Override
    public boolean test(Float value) {
        return value >= provider.min && value <= provider.max;
    }
}
