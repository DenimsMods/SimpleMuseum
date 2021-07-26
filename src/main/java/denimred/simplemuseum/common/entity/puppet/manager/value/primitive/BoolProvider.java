package denimred.simplemuseum.common.entity.puppet.manager.value.primitive;

import javax.annotation.Nullable;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.Callback;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import denimred.simplemuseum.common.util.IValueSerializer;
import denimred.simplemuseum.common.util.ValueSerializers;

public final class BoolProvider extends PuppetValueProvider<Boolean, BoolValue> {
    public BoolProvider(PuppetKey key, boolean defaultValue) {
        this(key, defaultValue, null);
    }

    public BoolProvider(PuppetKey key, boolean defaultValue, @Nullable Callback<Boolean> callback) {
        this(key, defaultValue, ValueSerializers.BOOLEAN, callback);
    }

    public BoolProvider(
            PuppetKey key,
            boolean defaultValue,
            IValueSerializer<Boolean> serializer,
            @Nullable Callback<Boolean> callback) {
        super(key, defaultValue, serializer, callback);
    }

    @Override
    public BoolValue provideFor(PuppetValueManager manager) {
        return new BoolValue(this, manager);
    }
}
