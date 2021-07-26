package denimred.simplemuseum.common.entity.puppet.manager.value;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;

public abstract class PuppetValue<T, P extends PuppetValueProvider<T, ? extends PuppetValue<T, P>>>
        implements Supplier<T>, Predicate<T> {
    public final P provider;
    public final PuppetValueManager manager;
    protected T value;

    protected PuppetValue(P provider, PuppetValueManager manager) {
        this.provider = provider;
        this.manager = manager;
        this.value = provider.defaultValue;
        if (manager.getValue(provider).isPresent()) {
            throw new IllegalStateException("Duplicate value initialization for " + provider.key);
        }
    }

    @Override
    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
        if (provider.dataKey != null) manager.data.set(provider.dataKey, value);
        if (provider.callback != null) provider.callback.accept(manager.puppet, value);
    }

    public void trySet(Object o) {
        final Class<T> type = provider.serializer.getType();
        if (type.isInstance(o)) {
            this.set(type.cast(o));
        }
    }

    public void modify(Function<T, T> modifier) {
        this.set(modifier.apply(this.get()));
    }

    public T getDefault() {
        return provider.defaultValue;
    }

    public boolean isDefault() {
        return this.get().equals(provider.defaultValue);
    }

    public void reset() {
        this.set(provider.defaultValue);
    }

    public boolean onDataChanged(DataParameter<?> key) {
        if (provider.dataKey != null && provider.dataKey.equals(key)) {
            this.set(manager.data.get(provider.dataKey));
            return true;
        }
        return false;
    }

    public void read(CompoundNBT tag) {
        if (tag.contains(provider.key.provider, provider.serializer.getTagId())) {
            try {
                this.set(provider.serializer.read(tag, provider.key.provider));
            } catch (Throwable t) {
                SimpleMuseum.LOGGER.warn("Failed to read from NBT data tag", t);
            }
        }
    }

    public void write(CompoundNBT tag) {
        if (value != provider.defaultValue) {
            provider.serializer.write(tag, provider.key.provider, value);
        }
    }

    @Override
    public boolean test(T t) {
        // Normally overridden
        return true;
    }

    public boolean matches(PuppetValue<?, ?> v) {
        return this.get().equals(v.get());
    }

    @SuppressWarnings("unchecked")
    public <C extends PuppetValue<?, ?>> C cast() {
        return (C) this;
    }
}
