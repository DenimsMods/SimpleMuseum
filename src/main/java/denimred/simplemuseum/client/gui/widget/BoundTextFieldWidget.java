package denimred.simplemuseum.client.gui.widget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;

import java.util.function.Supplier;

public class BoundTextFieldWidget extends TextFieldWidget {
    protected static final Color WHITE = Color.fromInt(0xFFFFFF);
    protected static final Color LIGHT_GREY = Color.fromInt(0xAAAAAA);
    protected static final Color DARK_GREY = Color.fromInt(0x777777);
    protected final Supplier<String> binder;
    protected String boundText = "";
    protected boolean paused = false;

    public BoundTextFieldWidget(
            FontRenderer font,
            int x,
            int y,
            int width,
            int height,
            ITextComponent title,
            Supplier<String> binder) {
        super(font, x, y, width, height, title);
        this.binder = binder;
        this.setTextFormatter(
                (s, i) -> {
                    if (s.isEmpty()) {
                        return IReorderingProcessor.fromString(s, Style.EMPTY);
                    } else {
                        final String[] split = s.split("\\.", -1);
                        if (split.length < 2) {
                            final char firstChar = s.charAt(0);
                            final boolean isLeft = firstChar == '+' || firstChar == '-';
                            return IReorderingProcessor.fromString(s, this.getStyle(isLeft));
                        } else {
                            final String left = split[0];
                            final String right = ".".concat(split[1]);
                            return IReorderingProcessor.func_242244_b(
                                    IReorderingProcessor.fromString(left, this.getStyle(true)),
                                    IReorderingProcessor.fromString(right, this.getStyle(false)));
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
        if (!paused && !this.getText().equals(boundText)) {
            this.reset();
        }
    }

    @Override
    protected void onTextChanged(String newText) {
        final boolean empty = newText.isEmpty();
        final char firstChar = empty ? '\u0000' : newText.charAt(0);
        if (!empty && firstChar != '-' && firstChar != '+') {
            this.setText("+".concat(newText));
        } else {
            paused = !newText.equals(boundText);
            super.onTextChanged(newText);
        }
    }

    public void reset() {
        this.setText(boundText);
        this.setCursorPositionZero();
        this.setSelectionPos(0);
    }

    public boolean isPaused() {
        return paused;
    }

    public Style getStyle(boolean left) {
        final Color color;
        if (left) {
            color = paused ? WHITE : LIGHT_GREY;
        } else {
            color = paused ? LIGHT_GREY : DARK_GREY;
        }
        return Style.EMPTY.setItalic(paused).setColor(color);
    }
}
