package denimred.simplemuseum.client.gui.widget.value;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.EntitySize;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Optional;

import denimred.simplemuseum.client.gui.screen.test.PuppetConfigScreen;
import denimred.simplemuseum.client.gui.widget.BetterTextFieldWidget;
import denimred.simplemuseum.client.gui.widget.DescriptiveButton;
import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.client.util.NumberUtil;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;
import denimred.simplemuseum.common.entity.puppet.manager.value.standard.EntitySizeValue;

public final class EntitySizeWidget extends ValueWidget<EntitySize, EntitySizeValue> {
    private final TextFieldWidget widthField;
    private final TextFieldWidget heightField;
    private final DescriptiveButton autoCalculateButton;

    public EntitySizeWidget(PuppetConfigScreen parent, PuppetValue<?, ?> value) {
        this(parent, 0, 0, 0, 35 + HEIGHT_MARGIN * 2, value.cast());
        heightMargin = HEIGHT_MARGIN;
    }

    public EntitySizeWidget(
            PuppetConfigScreen parent, int x, int y, int width, int height, EntitySizeValue value) {
        super(parent, x, y, width, height, value);
        autoCalculateButton =
                this.addChild(
                        new DescriptiveButton(
                                0,
                                0,
                                0,
                                20,
                                new StringTextComponent("Calculate"),
                                new StringTextComponent(
                                                "Automatically calculate the physical bounds of the puppet using the puppet's model geometry as a reference.")
                                        .mergeStyle(TextFormatting.GRAY),
                                this::autoCalculate,
                                parent::renderWidgetTooltip));
        widthField = this.addChild(new SizeHalfFieldWidget(false), true);
        heightField = this.addChild(new SizeHalfFieldWidget(true), true);
        this.setMaxWidth(298);
        this.detectAndSync();
    }

    @Override
    protected void recalculateChildren() {
        final int yPos = y + TITLE_OFFSET + heightMargin;
        final int widgetWidth = (width - 40) / 3;
        final Widget[] mainChildren = {autoCalculateButton, widthField, heightField};
        for (int i = 0; i < mainChildren.length; i++) {
            final Widget child = mainChildren[i];
            child.x = x + widgetWidth * i;
            child.y = yPos;
            child.setWidth(widgetWidth);
        }
        this.setChangeButtonsPos(x + widgetWidth * mainChildren.length, yPos);
        super.recalculateChildren();
    }

    private void autoCalculate(Button button) {
        final Optional<EntitySize> collisionBounds =
                ClientUtil.getPuppetBounds(value.manager.puppet).map(Pair::getFirst);
        if (collisionBounds.isPresent()) {
            value.set(collisionBounds.get());
            this.syncWithValue();
        }
    }

    @Override
    public void syncWithValue() {
        widthField.setText(String.valueOf(value.getWidth()));
        widthField.setCursorPositionZero();
        heightField.setText(String.valueOf(value.getHeight()));
        heightField.setCursorPositionZero();
    }

    @Override
    protected boolean hasChanged() {
        return !(value.getWidth() == original.width && value.getHeight() == original.height);
    }

    private final class SizeHalfFieldWidget extends BetterTextFieldWidget {
        private final boolean isHeight;

        public SizeHalfFieldWidget(boolean isHeight) {
            super(
                    ClientUtil.MC.fontRenderer,
                    0,
                    0,
                    0,
                    20,
                    isHeight
                            ? new StringTextComponent("Height")
                            : new StringTextComponent("Width"));
            this.isHeight = isHeight;
            this.setValidator(this::validate);
            this.setResponder(this::respond);
        }

        private boolean validate(String s) {
            return NumberUtil.isValidFloat(s, false);
        }

        private void respond(String s) {
            final Optional<Float> oF = NumberUtil.parseFloat(s);
            if (oF.isPresent()) {
                final float f = oF.get();
                if (isHeight) {
                    this.setTextColor(value.testHeight(f) ? TEXT_VALID : TEXT_INVALID);
                    value.setHeight(f);
                } else {
                    this.setTextColor(value.testWidth(f) ? TEXT_VALID : TEXT_INVALID);
                    value.setWidth(f);
                }
            } else {
                this.setTextColor(TEXT_ERROR);
            }
            EntitySizeWidget.this.detectChanges();
        }
    }
}
