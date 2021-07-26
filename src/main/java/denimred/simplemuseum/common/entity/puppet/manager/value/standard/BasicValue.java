package denimred.simplemuseum.common.entity.puppet.manager.value.standard;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

public final class BasicValue<T> extends PuppetValue<T, BasicProvider<T>> {
    protected BasicValue(BasicProvider<T> provider, PuppetValueManager manager) {
        super(provider, manager);
    }
}
