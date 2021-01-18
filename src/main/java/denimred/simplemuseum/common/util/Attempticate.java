package denimred.simplemuseum.common.util;

import java.util.function.Predicate;

/**
 * A type of {@linkplain Predicate} that simply returns true if execution was successful, or false
 * if an exception was thrown.
 */
@FunctionalInterface
public interface Attempticate<T> extends Predicate<T> {
    void attempt(T t) throws Throwable;

    @Override
    default boolean test(T t) {
        try {
            this.attempt(t);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
