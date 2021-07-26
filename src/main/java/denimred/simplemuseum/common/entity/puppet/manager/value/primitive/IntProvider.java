package denimred.simplemuseum.common.entity.puppet.manager.value.primitive;

import javax.annotation.Nullable;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.Callback;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import denimred.simplemuseum.common.util.IValueSerializer;
import denimred.simplemuseum.common.util.ValueSerializers;

public final class IntProvider extends PuppetValueProvider<Integer, IntValue> {
    public final int min;
    public final int max;

    public IntProvider(PuppetKey key, int defaultValue, int min, int max) {
        this(key, defaultValue, null, min, max);
    }

    public IntProvider(
            PuppetKey key,
            int defaultValue,
            @Nullable Callback<Integer> callback,
            int min,
            int max) {
        this(key, defaultValue, ValueSerializers.INTEGER, callback, min, max);
    }

    public IntProvider(
            PuppetKey key,
            int defaultValue,
            IValueSerializer<Integer> serializer,
            @Nullable Callback<Integer> callback,
            int min,
            int max) {
        super(key, defaultValue, serializer, callback);
        if (min > max) {
            throw new IllegalArgumentException(
                    String.format("Invalid range, min (%d) > max (%d)", min, max));
        }
        this.min = min;
        this.max = max;
    }

    @Override
    public IntValue provideFor(PuppetValueManager manager) {
        return new IntValue(this, manager);
    }
}
