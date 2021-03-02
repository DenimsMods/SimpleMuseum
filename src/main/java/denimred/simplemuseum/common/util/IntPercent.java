package denimred.simplemuseum.common.util;

public class IntPercent {
    private final int min;
    private final int max;
    private int intVal;
    private float floatVal;

    public IntPercent(int min, int max) {
        this.min = min;
        this.max = max;
        this.set(min);
    }

    public int asInt() {
        return intVal;
    }

    public float asFloat() {
        return floatVal;
    }

    public IntPercent set(int value) {
        intVal = Math.max(min, Math.min(max, value));
        floatVal = (float) intVal / 100.0F;
        return this;
    }

    public IntPercent set(float value) {
        return this.set(Math.round(value * 100.0F));
    }
}
