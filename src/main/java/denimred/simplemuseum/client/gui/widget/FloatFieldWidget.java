package denimred.simplemuseum.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.Optional;

import denimred.simplemuseum.client.util.NumberUtil;

public class FloatFieldWidget extends BetterTextFieldWidget {
    private final float min;
    private final float max;
    private float floatValue;

    public FloatFieldWidget(Font font, int x, int y, int width, int height, Component title) {
        this(font, x, y, width, height, title, Float.MIN_VALUE, Float.MAX_VALUE);
    }

    public FloatFieldWidget(Font font, int x, int y, int width, int height, Component title, float min, float max) {
        super(font, x, y, width, height, title);
        this.min = min;
        this.max = max;
        this.setFilter(NumberUtil::isValidFloat);
        this.setResponder(this::respond);
    }

    public float getFloatValue() {
        return floatValue;
    }

    public boolean isValueInRange() {
        return floatValue <= max && floatValue >= min;
    }

    private void respond(String s) {
        final Optional<Float> oI = NumberUtil.parseFloat(s);
        if (oI.isPresent()) {
            floatValue = oI.get();
            this.setTextColor(this.isValueInRange() ? TEXT_VALID : TEXT_INVALID);
        } else {
            this.setTextColor(TEXT_ERROR);
        }
    }
}