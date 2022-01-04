package denimred.simplemuseum.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

import java.util.function.Supplier;

@Deprecated
public class BoundTextFieldWidget extends EditBox {
    protected static final TextColor WHITE = TextColor.fromRgb(0xFFFFFF);
    protected static final TextColor LIGHT_GREY = TextColor.fromRgb(0xAAAAAA);
    protected static final TextColor DARK_GREY = TextColor.fromRgb(0x777777);
    protected final Supplier<String> binder;
    protected String boundText = "";
    protected boolean paused = false;

    public BoundTextFieldWidget(
            Font font,
            int x,
            int y,
            int width,
            int height,
            Component title,
            Supplier<String> binder) {
        super(font, x, y, width, height, title);
        this.binder = binder;
        this.setFormatter(
                (s, i) -> {
                    if (s.isEmpty()) {
                        return FormattedCharSequence.forward(s, Style.EMPTY);
                    } else {
                        final String[] split = s.split("\\.", -1);
                        if (split.length < 2) {
                            final char firstChar = s.charAt(0);
                            final boolean isLeft = firstChar == '+' || firstChar == '-';
                            return FormattedCharSequence.forward(s, this.getStyle(isLeft));
                        } else {
                            final String left = split[0];
                            final String right = ".".concat(split[1]);
                            return FormattedCharSequence.fromPair(
                                    FormattedCharSequence.forward(left, this.getStyle(true)),
                                    FormattedCharSequence.forward(right, this.getStyle(false)));
                        }
                    }
                });
        this.bind();
    }

    @Override
    public void tick() {
        super.tick();
        this.bind();
    }

    protected void bind() {
        boundText = binder.get();
        if (!paused && !this.getValue().equals(boundText)) {
            this.reset();
        }
    }

    @Override
    protected void onValueChange(String newText) {
        final boolean empty = newText.isEmpty();
        final char firstChar = empty ? '\u0000' : newText.charAt(0);
        if (!empty && firstChar != '-' && firstChar != '+') {
            this.setValue("+".concat(newText));
        } else {
            paused = !newText.equals(boundText);
            super.onValueChange(newText);
        }
    }

    public void reset() {
        this.setValue(boundText);
        this.moveCursorToStart();
        this.setHighlightPos(0);
    }

    public boolean isPaused() {
        return paused;
    }

    public Style getStyle(boolean left) {
        final TextColor color;
        if (left) {
            color = paused ? WHITE : LIGHT_GREY;
        } else {
            color = paused ? LIGHT_GREY : DARK_GREY;
        }
        return Style.EMPTY.withItalic(paused).withColor(color);
    }
}
