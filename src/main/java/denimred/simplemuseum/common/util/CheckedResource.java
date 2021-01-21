package denimred.simplemuseum.common.util;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;

public class CheckedResource<T> {
    private final Predicate<T> validator;
    private final T fallback;
    private T current;
    @Nullable private T cached;

    public CheckedResource(T fallback, Attempticate<T> validator) {
        this(fallback, (Predicate<T>) validator);
    }

    public CheckedResource(T fallback, Predicate<T> validator) {
        this.fallback = fallback;
        this.validator = validator;
        current = fallback;
    }

    public T getDirect() {
        return current;
    }

    public T getSafe() {
        if (cached != current && cached != fallback || cached == null) {
            if (validator.test(current)) {
                cached = current;
            } else {
                SimpleMuseum.LOGGER.debug(
                        String.format("Resource '%s' is invalid (may not exist)", current));
                cached = fallback;
            }
        }
        return cached;
    }

    public void set(T t) {
        current = t;
        cached = null;
    }

    public boolean validate(T t) {
        return validator.test(t);
    }

    public void clearCached() {
        cached = null;
    }
}
