package denimred.simplemuseum.common.entity.puppet.manager.value.standard;

import net.minecraft.entity.EntitySize;

import javax.annotation.Nullable;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.Callback;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import denimred.simplemuseum.common.util.IValueSerializer;
import denimred.simplemuseum.common.util.ValueSerializers;

public final class EntitySizeProvider extends PuppetValueProvider<EntitySize, EntitySizeValue> {
    protected final EntitySize min;
    protected final EntitySize max;

    public EntitySizeProvider(
            PuppetKey key, EntitySize defaultValue, EntitySize min, EntitySize max) {
        this(key, defaultValue, null, min, max);
    }

    public EntitySizeProvider(
            PuppetKey key,
            EntitySize defaultValue,
            @Nullable Callback<EntitySize> callback,
            EntitySize min,
            EntitySize max) {
        this(key, defaultValue, ValueSerializers.ENTITY_SIZE, callback, min, max);
    }

    public EntitySizeProvider(
            PuppetKey key,
            EntitySize defaultValue,
            IValueSerializer<EntitySize> serializer,
            @Nullable Callback<EntitySize> callback,
            EntitySize min,
            EntitySize max) {
        super(key, defaultValue, serializer, callback);
        this.min = min;
        this.max = max;
    }

    @Override
    public EntitySizeValue provideFor(PuppetValueManager manager) {
        return new EntitySizeValue(this, manager);
    }
}
