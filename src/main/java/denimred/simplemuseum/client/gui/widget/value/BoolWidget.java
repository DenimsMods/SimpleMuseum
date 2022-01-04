package denimred.simplemuseum.client.gui.widget.value;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import denimred.simplemuseum.client.gui.screen.PuppetConfigScreen;
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
        final MutableComponent msgTrue =
                new TranslatableComponent("True").withStyle(ChatFormatting.GREEN);
        final MutableComponent msgFalse =
                new TranslatableComponent("False").withStyle(ChatFormatting.RED);
        toggleButton =
                this.addChild(
                        new ExtendedButton(
                                0,
                                0,
                                0,
                                20,
                                this.valueRef.get()
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
        valueRef.set(!valueRef.get());
        this.detectAndSync();
    }

    @Override
    public void syncWithValue() {
        final MutableComponent msgTrue =
                new TranslatableComponent("True").withStyle(ChatFormatting.GREEN);
        final MutableComponent msgFalse =
                new TranslatableComponent("False").withStyle(ChatFormatting.RED);
        toggleButton.setMessage(
                valueRef.get()
                        ? msgTrue.setStyle(msgTrue.getStyle())
                        : msgFalse.setStyle(msgFalse.getStyle()));
    }
}
