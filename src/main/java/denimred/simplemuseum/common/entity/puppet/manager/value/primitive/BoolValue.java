package denimred.simplemuseum.common.entity.puppet.manager.value.primitive;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

public final class BoolValue extends PuppetValue<Boolean, BoolProvider> {
    protected BoolValue(BoolProvider provider, PuppetValueManager manager) {
        super(provider, manager);
    }
}
