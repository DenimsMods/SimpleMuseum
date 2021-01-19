package denimred.simplemuseum.common.util;

import java.util.function.Predicate;

public class CheckedResource<T> {
    private final T fallback;
    private final Predicate<T> checker;
    private T current;
    private T lastGood;

    public CheckedResource(T fallback, Attempticate<T> checker) {
        this(fallback, (Predicate<T>) checker);
    }

    public CheckedResource(T fallback, Predicate<T> checker) {
        this.fallback = fallback;
        this.checker = checker;
        current = fallback;
    }

    public T getFallback() {
        return fallback;
    }

    public T getDirect() {
        return current;
    }

    public T getSafe() {
        if (lastGood != current) {
            if (lastGood != fallback) {
                if (checker.test(current)) {
                    lastGood = current;
                } else {
//                    SimpleMuseum.LOGGER.warn(
//                            String.format(
//                                    "Resource '%s' of type '%s' doesn't exist",
//                                    current, current.getClass().getSimpleName()));
                    lastGood = fallback;
                }
            }
            return lastGood;
        }
        return this.getDirect();
    }

    public void set(T t) {
        current = t;
        lastGood = null;
    }

    public boolean check(T t) {
        return checker.test(t);
    }
}
