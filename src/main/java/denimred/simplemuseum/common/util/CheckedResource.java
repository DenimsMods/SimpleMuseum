package denimred.simplemuseum.common.util;

import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

public class CheckedResource<T> {
    private final T fallback;
    private final Predicate<T> validator;
    @Nullable private final Consumer<T> callback;
    private T direct;
    @Nullable private T cached;
    private boolean valid;

    public CheckedResource(T fallback, Attempticate<T> validator, @Nullable Consumer<T> callback) {
        this(fallback, (Predicate<T>) validator, callback);
    }

    public CheckedResource(T fallback, Predicate<T> validator, @Nullable Consumer<T> callback) {
        this.fallback = fallback;
        this.validator = validator;
        this.callback = callback;
        direct = fallback;
    }

    public T getFallback() {
        return fallback;
    }

    public T getDirect() {
        return direct;
    }

    public T getSafe() {
        if (cached != direct && cached != fallback || cached == null) {
            valid = validator.test(direct);
            if (valid) {
                cached = direct;
            } else {
                cached = fallback;
            }
        }
        return cached;
    }

    public boolean isValid() {
        // Sanity check; validity can only be accurate when the cached value exists
        if (cached == null) {
            this.getSafe();
        }
        return valid;
    }

    public void set(T t) {
        direct = t;
        cached = null;
        if (callback != null) callback.accept(t);
    }

    public boolean validate(T t) {
        return validator.test(t);
    }

    public void clearCache() {
        cached = null;
    }
}
