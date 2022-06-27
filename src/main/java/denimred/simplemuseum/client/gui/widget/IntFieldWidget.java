package denimred.simplemuseum.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.Optional;

import denimred.simplemuseum.client.util.NumberUtil;

public class IntFieldWidget extends BetterTextFieldWidget {
    private final int min;
    private final int max;
    private int intValue;

    public IntFieldWidget(Font font, int x, int y, int width, int height, Component title) {
        this(font, x, y, width, height, title, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public IntFieldWidget(Font font, int x, int y, int width, int height, Component title, int min, int max) {
        super(font, x, y, width, height, title);
        this.min = min;
        this.max = max;
        this.setFilter(NumberUtil::isValidInt);
        this.setResponder(this::respond);
    }

    public int getIntValue() {
        return intValue;
    }

    public boolean isValueInRange() {
        return intValue <= max && intValue >= min;
    }

    private void respond(String s) {
        final Optional<Integer> oI = NumberUtil.parseInt(s);
        if (oI.isPresent()) {
            intValue = oI.get();
            this.setTextColor(this.isValueInRange() ? TEXT_VALID : TEXT_INVALID);
        } else {
            this.setTextColor(TEXT_ERROR);
        }
    }
}