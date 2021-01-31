package denimred.simplemuseum.common.util;

import java.util.function.Predicate;

import javax.annotation.Nullable;

public class CheckedResource<T> {
    private final Predicate<T> validator;
    private final T fallback;
    private T current;
    @Nullable private T cached;
    private boolean valid;

    public CheckedResource(T fallback, Attempticate<T> validator) {
        this(fallback, (Predicate<T>) validator);
    }

    public CheckedResource(T fallback, Predicate<T> validator) {
        this.fallback = fallback;
        this.validator = validator;
        current = fallback;
    }

    public T getFallback() {
        return fallback;
    }

    public T getDirect() {
        return current;
    }

    public T getSafe() {
        if (cached != current && cached != fallback || cached == null) {
            valid = validator.test(current);
            if (valid) {
                cached = current;
            } else {
                cached = fallback;
            }
        }
        return cached;
    }

    public boolean isInvalid() {
        return !valid;
    }

    public void set(T t) {
        current = t;
        cached = null;
        valid = true; // Eh, semantically wrong but practically fine
    }

    public boolean validate(T t) {
        return validator.test(t);
    }

    public void clearCached() {
        cached = null;
    }
}
