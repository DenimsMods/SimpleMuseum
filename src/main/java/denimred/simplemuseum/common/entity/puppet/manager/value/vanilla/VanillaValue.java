package denimred.simplemuseum.common.entity.puppet.manager.value.vanilla;

import net.minecraft.nbt.CompoundTag;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

public final class VanillaValue<T> extends PuppetValue<T, VanillaProvider<T>> {
    protected VanillaValue(VanillaProvider<T> provider, PuppetValueManager manager) {
        super(provider, manager);
        provider.setter.accept(manager.puppet, provider.defaultValue);
    }

    @Override
    public T get() {
        return provider.getter.apply(manager.puppet);
    }

    @Override
    public void set(T value) {
        provider.setter.accept(manager.puppet, value);
        if (provider.dataKey != null) {
            manager.data.set(provider.dataKey, value);
        }
    }

    @Override
    public void read(CompoundTag tag) {
        // no-op, handled by vanilla
    }

    @Override
    public void write(CompoundTag tag) {
        // no-op, handled by vanilla
    }
}
