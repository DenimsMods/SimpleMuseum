package denimred.simplemuseum.common.entity.puppet.manager.value.correcting;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

public final class CorrectingValue<T> extends PuppetValue<T, CorrectingProvider<T>> {
    protected CorrectingValue(CorrectingProvider<T> provider, PuppetValueManager manager) {
        super(provider, manager);
    }

    @Override
    public void set(T value) {
        if (this.test(value)) {
            super.set(value);
        } else {
            super.set(provider.corrector.apply(manager.puppet, value));
        }
    }

    @Override
    public boolean test(T t) {
        return provider.validator.test(manager.puppet, t);
    }
}
