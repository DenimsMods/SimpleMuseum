package denimred.simplemuseum.client.gui.widget.value;

import java.util.Optional;

import denimred.simplemuseum.client.gui.screen.PuppetConfigScreen;
import denimred.simplemuseum.client.gui.widget.BetterTextFieldWidget;
import denimred.simplemuseum.client.util.NumberUtil;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

public final class IntWidget extends ValueWidget<Integer, PuppetValue<Integer, ?>> {
    private final IntTextField intTextField;

    public IntWidget(PuppetConfigScreen parent, PuppetValue<?, ?> value) {
        this(parent, 0, 0, 0, 35 + HEIGHT_MARGIN * 2, value.cast());
        heightMargin = HEIGHT_MARGIN;
    }

    public IntWidget(
            PuppetConfigScreen parent,
            int x,
            int y,
            int width,
            int height,
            PuppetValue<Integer, ?> value) {
        super(parent, x, y, width, height, value);
        intTextField = this.addChild(new IntTextField(), true);
        this.setMaxWidth(100);
        this.detectAndSync();
    }

    @Override
    protected void recalculateChildren() {
        final int yPos = y + TITLE_OFFSET + heightMargin;
        final int widgetWidth = width - 40;
        intTextField.x = x;
        intTextField.y = yPos;
        intTextField.setWidth(widgetWidth);
        this.setChangeButtonsPos(x + widgetWidth, yPos);
        super.recalculateChildren();
    }

    @Override
    public void syncWithValue() {
        intTextField.setText(String.valueOf(value.get()));
        intTextField.setCursorPositionZero();
    }

    private final class IntTextField extends BetterTextFieldWidget {
        public IntTextField() {
            super(MC.fontRenderer, 0, 0, 0, 20, IntWidget.this.message);
            this.setValidator(this::validate);
            this.setResponder(this::respond);
        }

        private boolean validate(String s) {
            return NumberUtil.isValidInt(s, false);
        }

        private void respond(String s) {
            final Optional<Integer> oI = NumberUtil.parseInt(s);
            if (oI.isPresent()) {
                final int i = oI.get();
                this.setTextColor(value.test(i) ? TEXT_VALID : TEXT_INVALID);
                value.set(i);
            } else {
                this.setTextColor(TEXT_ERROR);
            }
            IntWidget.this.detectChanges();
        }
    }
}
