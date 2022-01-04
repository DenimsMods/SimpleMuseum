package denimred.simplemuseum.client.gui.widget.value;

import java.util.Optional;

import denimred.simplemuseum.client.gui.screen.PuppetConfigScreen;
import denimred.simplemuseum.client.gui.widget.BetterTextFieldWidget;
import denimred.simplemuseum.client.util.NumberUtil;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

public final class FloatWidget extends ValueWidget<Float, PuppetValue<Float, ?>> {
    private final FloatTextField floatTextField;

    public FloatWidget(PuppetConfigScreen parent, PuppetValue<?, ?> value) {
        this(parent, 0, 0, 0, 35 + HEIGHT_MARGIN * 2, value.cast());
        heightMargin = HEIGHT_MARGIN;
    }

    public FloatWidget(
            PuppetConfigScreen parent,
            int x,
            int y,
            int width,
            int height,
            PuppetValue<Float, ?> value) {
        super(parent, x, y, width, height, value);
        floatTextField = this.addChild(new FloatTextField(), true);
        this.setMaxWidth(100);
        this.detectAndSync();
    }

    @Override
    protected void recalculateChildren() {
        final int yPos = y + TITLE_OFFSET + heightMargin;
        final int widgetWidth = width - 40;
        floatTextField.x = x;
        floatTextField.y = yPos;
        floatTextField.setWidth(widgetWidth);
        this.setChangeButtonsPos(x + widgetWidth, yPos);
        super.recalculateChildren();
    }

    @Override
    public void syncWithValue() {
        floatTextField.setValue(String.valueOf(valueRef.get()));
        floatTextField.moveCursorToStart();
    }

    private final class FloatTextField extends BetterTextFieldWidget {
        public FloatTextField() {
            super(MC.font, 0, 0, 0, 20, FloatWidget.this.message);
            this.setFilter(this::validate);
            this.setResponder(this::respond);
        }

        private boolean validate(String s) {
            return NumberUtil.isValidFloat(s, false);
        }

        private void respond(String s) {
            final Optional<Float> oI = NumberUtil.parseFloat(s);
            if (oI.isPresent()) {
                final float i = oI.get();
                this.setTextColor(valueRef.test(i) ? TEXT_VALID : TEXT_INVALID);
                valueRef.set(i);
            } else {
                this.setTextColor(TEXT_ERROR);
            }
            FloatWidget.this.detectChanges();
        }
    }
}
