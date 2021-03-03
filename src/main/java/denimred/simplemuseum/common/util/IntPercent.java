package denimred.simplemuseum.common.util;

import java.util.function.Consumer;

import javax.annotation.Nullable;

public class IntPercent {
    private final int min;
    private final int max;
    @Nullable private final Consumer<Integer> callback;
    private int intVal;
    private float floatVal;

    public IntPercent(int min, int max, int defaultValue, @Nullable Consumer<Integer> callback) {
        this.min = min;
        this.max = max;
        // Is it arguably better to set the default before assigning the callback?
        this.set(defaultValue);
        this.callback = callback;
    }

    public int asInt() {
        return intVal;
    }

    public float asFloat() {
        return floatVal;
    }

    public void set(float value) {
        this.set(Math.round(value * 100.0F));
    }

    public void set(int value) {
        intVal = Math.max(min, Math.min(max, value));
        floatVal = (float) intVal / 100.0F;
        if (callback != null) callback.accept(intVal);
    }
}
