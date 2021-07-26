package denimred.simplemuseum.common.entity.puppet.manager.value.primitive;

import javax.annotation.Nullable;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.Callback;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import denimred.simplemuseum.common.util.IValueSerializer;
import denimred.simplemuseum.common.util.ValueSerializers;

public final class FloatProvider extends PuppetValueProvider<Float, FloatValue> {
    public final float min;
    public final float max;

    public FloatProvider(PuppetKey key, float defaultValue, float min, float max) {
        this(key, defaultValue, null, min, max);
    }

    public FloatProvider(
            PuppetKey key,
            float defaultValue,
            @Nullable Callback<Float> callback,
            float min,
            float max) {
        this(key, defaultValue, ValueSerializers.FLOAT, callback, min, max);
    }

    public FloatProvider(
            PuppetKey key,
            float defaultValue,
            IValueSerializer<Float> serializer,
            @Nullable Callback<Float> callback,
            float min,
            float max) {
        super(key, defaultValue, serializer, callback);
        if (min > max) {
            throw new IllegalArgumentException(
                    String.format("Invalid range, min (%f) > max (%f)", min, max));
        }
        this.min = min;
        this.max = max;
    }

    @Override
    public FloatValue provideFor(PuppetValueManager manager) {
        return new FloatValue(this, manager);
    }
}
