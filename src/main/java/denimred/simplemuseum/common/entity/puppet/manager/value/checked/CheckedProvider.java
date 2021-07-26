package denimred.simplemuseum.common.entity.puppet.manager.value.checked;

import javax.annotation.Nullable;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.Callback;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import denimred.simplemuseum.common.entity.puppet.manager.value.Validator;
import denimred.simplemuseum.common.util.IValueSerializer;
import denimred.simplemuseum.common.util.ValueSerializers;

public final class CheckedProvider<T> extends PuppetValueProvider<T, CheckedValue<T>> {
    public final Validator<T> validator;

    public CheckedProvider(PuppetKey key, T defaultValue, Validator<T> validator) {
        this(key, defaultValue, null, validator);
    }

    public CheckedProvider(
            PuppetKey key, T defaultValue, @Nullable Callback<T> callback, Validator<T> validator) {
        this(key, defaultValue, ValueSerializers.lazyGuess(defaultValue), callback, validator);
    }

    public CheckedProvider(
            PuppetKey key,
            T defaultValue,
            IValueSerializer<T> serializer,
            @Nullable Callback<T> callback,
            Validator<T> validator) {
        super(key, defaultValue, serializer, callback);
        this.validator = validator;
    }

    @Override
    public CheckedValue<T> provideFor(PuppetValueManager manager) {
        return new CheckedValue<>(this, manager);
    }
}
