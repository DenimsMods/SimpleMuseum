package denimred.simplemuseum.common.entity.puppet.manager.value.vanilla;

import net.minecraft.network.syncher.EntityDataAccessor;

import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import denimred.simplemuseum.common.util.IValueSerializer;
import denimred.simplemuseum.common.util.ValueSerializers;

public final class VanillaProvider<T> extends PuppetValueProvider<T, VanillaValue<T>> {
    protected final Function<PuppetEntity, T> getter;
    protected final BiConsumer<PuppetEntity, T> setter;

    public VanillaProvider(
            PuppetKey key,
            T defaultValue,
            Function<PuppetEntity, T> getter,
            BiConsumer<PuppetEntity, T> setter,
            boolean syncData) {
        this(key, defaultValue, getter, setter, syncData, ValueSerializers.lazyGuess(defaultValue));
    }

    public VanillaProvider(
            PuppetKey key,
            T defaultValue,
            Function<PuppetEntity, T> getter,
            BiConsumer<PuppetEntity, T> setter,
            boolean syncData,
            IValueSerializer<T> serializer) {
        // Callback should always be null since it will never be called from vanilla code anyways
        super(key, defaultValue, serializer, null, createDataKey(syncData, serializer));
        this.getter = getter;
        this.setter = setter;
    }

    @Nullable
    private static <T> EntityDataAccessor<T> createDataKey(
            boolean syncData, IValueSerializer<T> serializer) {
        return syncData ? PuppetEntity.createKeyContextual(serializer) : null;
    }

    @Override
    public VanillaValue<T> provideFor(PuppetValueManager manager) {
        return new VanillaValue<>(this, manager);
    }
}
