package denimred.simplemuseum.common.entity.puppet.manager.value.standard;

import javax.annotation.Nullable;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.Callback;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import denimred.simplemuseum.common.util.IValueSerializer;
import denimred.simplemuseum.common.util.ValueSerializers;

public final class BasicProvider<T> extends PuppetValueProvider<T, BasicValue<T>> {
    public BasicProvider(PuppetKey key, T defaultValue) {
        this(key, defaultValue, (Callback<T>) null);
    }

    public BasicProvider(PuppetKey key, T defaultValue, @Nullable Callback<T> callback) {
        this(key, defaultValue, ValueSerializers.lazyGuess(defaultValue), callback);
    }

    public BasicProvider(PuppetKey key, T defaultValue, IValueSerializer<T> serializer) {
        super(key, defaultValue, serializer, null);
    }

    public BasicProvider(
            PuppetKey key,
            T defaultValue,
            IValueSerializer<T> serializer,
            @Nullable Callback<T> callback) {
        super(key, defaultValue, serializer, callback);
    }

    @Override
    public BasicValue<T> provideFor(PuppetValueManager manager) {
        return new BasicValue<>(this, manager);
    }
}
