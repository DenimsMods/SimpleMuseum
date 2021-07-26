package denimred.simplemuseum.common.entity.puppet.manager.value.checked;

import javax.annotation.Nullable;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

public final class CheckedValue<T> extends PuppetValue<T, CheckedProvider<T>> {
    @Nullable protected T cached;
    protected boolean valid;

    protected CheckedValue(CheckedProvider<T> provider, PuppetValueManager manager) {
        super(provider, manager);
    }

    @Override
    public T get() {
        return value;
    }

    public T getSafe() {
        if (cached == null || cached != value && cached != provider.defaultValue) {
            valid = this.test(value);
            cached = valid ? value : provider.defaultValue;
        }
        return cached;
    }

    @Override
    public void set(T value) {
        this.invalidateCache();
        super.set(value);
    }

    public boolean isValid() {
        // Sanity check; validity can only be assured when the cached value exists
        if (cached == null) this.getSafe();
        return valid;
    }

    @Override
    public boolean test(T t) {
        return provider.validator.test(manager.puppet, t);
    }

    public void invalidateCache() {
        cached = null;
    }
}
