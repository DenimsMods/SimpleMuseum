package denimred.simplemuseum.common.entity.puppet.manager.value.standard;

import net.minecraft.world.entity.EntityDimensions;

import javax.annotation.Nullable;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.Callback;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import denimred.simplemuseum.common.util.IValueSerializer;
import denimred.simplemuseum.common.util.ValueSerializers;

public final class EntitySizeProvider
        extends PuppetValueProvider<EntityDimensions, EntitySizeValue> {
    protected final EntityDimensions min;
    protected final EntityDimensions max;

    public EntitySizeProvider(
            PuppetKey key,
            EntityDimensions defaultValue,
            EntityDimensions min,
            EntityDimensions max) {
        this(key, defaultValue, null, min, max);
    }

    public EntitySizeProvider(
            PuppetKey key,
            EntityDimensions defaultValue,
            @Nullable Callback<EntityDimensions> callback,
            EntityDimensions min,
            EntityDimensions max) {
        this(key, defaultValue, ValueSerializers.ENTITY_SIZE, callback, min, max);
    }

    public EntitySizeProvider(
            PuppetKey key,
            EntityDimensions defaultValue,
            IValueSerializer<EntityDimensions> serializer,
            @Nullable Callback<EntityDimensions> callback,
            EntityDimensions min,
            EntityDimensions max) {
        super(key, defaultValue, serializer, callback);
        this.min = min;
        this.max = max;
    }

    @Override
    public EntitySizeValue provideFor(PuppetValueManager manager) {
        return new EntitySizeValue(this, manager);
    }
}
