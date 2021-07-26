package denimred.simplemuseum.client.gui.widget.value;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import denimred.simplemuseum.client.gui.screen.test.PuppetConfigScreen;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

public final class BoolWidget extends ValueWidget<Boolean, PuppetValue<Boolean, ?>> {
    private final ExtendedButton toggleButton;

    public BoolWidget(PuppetConfigScreen parent, PuppetValue<?, ?> value) {
        this(parent, 0, 0, 0, 35 + HEIGHT_MARGIN * 2, value.cast());
        heightMargin = HEIGHT_MARGIN;
    }

    public BoolWidget(
            PuppetConfigScreen parent,
            int x,
            int y,
            int width,
            int height,
            PuppetValue<Boolean, ?> value) {
        super(parent, x, y, width, height, value);
        final IFormattableTextComponent msgTrue =
                new TranslationTextComponent("True").mergeStyle(TextFormatting.GREEN);
        final IFormattableTextComponent msgFalse =
                new TranslationTextComponent("False").mergeStyle(TextFormatting.RED);
        toggleButton =
                this.addChild(
                        new ExtendedButton(
                                0,
                                0,
                                0,
                                20,
                                this.value.get()
                                        ? msgTrue.setStyle(msgTrue.getStyle())
                                        : msgFalse.setStyle(msgFalse.getStyle()),
                                this::toggle));
        this.setMaxWidth(100);
        this.detectAndSync();
    }

    @Override
    protected void recalculateChildren() {
        final int yPos = y + TITLE_OFFSET + heightMargin;
        final int widgetWidth = width - 40;
        toggleButton.x = x;
        toggleButton.y = yPos;
        toggleButton.setWidth(widgetWidth);
        this.setChangeButtonsPos(x + widgetWidth, yPos);
        super.recalculateChildren();
    }

    private void toggle(Button button) {
        value.set(!value.get());
        this.detectAndSync();
    }

    @Override
    public void syncWithValue() {
        final IFormattableTextComponent msgTrue =
                new TranslationTextComponent("True").mergeStyle(TextFormatting.GREEN);
        final IFormattableTextComponent msgFalse =
                new TranslationTextComponent("False").mergeStyle(TextFormatting.RED);
        toggleButton.setMessage(
                value.get()
                        ? msgTrue.setStyle(msgTrue.getStyle())
                        : msgFalse.setStyle(msgFalse.getStyle()));
    }
}
