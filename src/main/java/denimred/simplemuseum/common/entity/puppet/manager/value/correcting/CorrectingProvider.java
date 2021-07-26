package denimred.simplemuseum.common.entity.puppet.manager.value.correcting;

import javax.annotation.Nullable;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.Callback;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import denimred.simplemuseum.common.entity.puppet.manager.value.Validator;
import denimred.simplemuseum.common.util.IValueSerializer;
import denimred.simplemuseum.common.util.ValueSerializers;

public final class CorrectingProvider<T> extends PuppetValueProvider<T, CorrectingValue<T>> {
    public final Validator<T> validator;
    public final Corrector<T> corrector;

    public CorrectingProvider(
            PuppetKey key, T defaultValue, Validator<T> validator, Corrector<T> corrector) {
        this(key, defaultValue, null, validator, corrector);
    }

    public CorrectingProvider(
            PuppetKey key,
            T defaultValue,
            @Nullable Callback<T> callback,
            Validator<T> validator,
            Corrector<T> corrector) {
        this(
                key,
                defaultValue,
                ValueSerializers.lazyGuess(defaultValue),
                callback,
                validator,
                corrector);
    }

    public CorrectingProvider(
            PuppetKey key,
            T defaultValue,
            IValueSerializer<T> serializer,
            @Nullable Callback<T> callback,
            Validator<T> validator,
            Corrector<T> corrector) {
        super(key, defaultValue, serializer, callback);
        this.validator = validator;
        this.corrector = corrector;
    }

    @Override
    public CorrectingValue<T> provideFor(PuppetValueManager manager) {
        return new CorrectingValue<>(this, manager);
    }
}
