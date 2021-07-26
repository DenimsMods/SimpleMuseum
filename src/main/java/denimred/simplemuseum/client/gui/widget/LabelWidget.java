package denimred.simplemuseum.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.CharacterManager;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

public class LabelWidget extends Widget {
    public final FontRenderer font;
    private final AnchorX anchorX;
    private final AnchorY anchorY;
    private List<? extends ITextProperties> texts;
    @Nullable private Tooltip tooltip;
    private int left;
    private int right;
    private int top;
    private int bottom;
    private int lastX = x;
    private int lastY = y;

    public LabelWidget(
            int x,
            int y,
            FontRenderer font,
            AnchorX anchorX,
            AnchorY anchorY,
            ITextProperties... texts) {
        this(x, y, font, anchorX, anchorY, Arrays.asList(texts));
    }

    public LabelWidget(
            int x,
            int y,
            FontRenderer font,
            AnchorX anchorX,
            AnchorY anchorY,
            List<? extends ITextProperties> texts) {
        super(x, y, 0, 0, StringTextComponent.EMPTY);
        this.font = font;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.texts = texts;
        this.recalculateBounds();
    }

    private static int getMaxWidth(FontRenderer font, List<? extends ITextProperties> messages) {
        int width = 0;
        for (ITextProperties message : messages) {
            width = Math.max(width, font.getStringPropertyWidth(message));
        }
        return width;
    }

    public void setTooltip(@Nullable Tooltip tooltip) {
        this.tooltip = tooltip;
    }

    private void recalculateBounds() {
        this.setWidth(getMaxWidth(font, texts));
        this.setHeight(font.FONT_HEIGHT * texts.size());
        switch (anchorX) {
            case LEFT:
                left = x;
                right = x + width;
                break;
            case RIGHT:
                left = x - width;
                right = x;
                break;
            case CENTER:
                left = x - width / 2;
                right = x + width / 2;
                break;
        }
        switch (anchorY) {
            case TOP:
                top = y;
                bottom = y + height;
                break;
            case BOTTOM:
                top = y - height;
                bottom = y;
                break;
            case CENTER:
                top = y - height / 2;
                bottom = y + height / 2;
                break;
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            if (lastX != x || lastY != y) {
                this.recalculateBounds();
                lastX = x;
                lastY = y;
            }

            isHovered = mouseX >= left && mouseY >= top && mouseX < right && mouseY < bottom;

            if (wasHovered != this.isHovered()) {
                if (this.isHovered()) {
                    if (focused) {
                        this.queueNarration(200);
                    } else {
                        this.queueNarration(750);
                    }
                } else {
                    nextNarration = Long.MAX_VALUE;
                }
            }

            if (visible) {
                this.renderWidget(matrixStack, mouseX, mouseY, partialTicks);
            }

            this.narrate();
            wasHovered = this.isHovered();
        }
    }

    @Override
    public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        for (int i = 0, count = texts.size(); i < count; i++) {
            final ITextProperties text = texts.get(i);
            final int w = font.getStringPropertyWidth(text);
            final int h = font.FONT_HEIGHT;

            final int x;
            switch (anchorX) {
                case LEFT:
                    x = left;
                    break;
                case RIGHT:
                    x = right - w;
                    break;
                case CENTER:
                    x = this.x - w / 2;
                    break;
                default:
                    x = 0;
                    break;
            }

            final int y;
            switch (anchorY) {
                case TOP:
                    y = top + h * i;
                    break;
                case BOTTOM:
                    y = bottom - h * (count - i);
                    break;
                case CENTER:
                    y = this.y - count * h / 2 + h * i;
                    break;
                default:
                    y = 0;
                    break;
            }

            font.drawTextWithShadow(
                    matrixStack, LanguageMap.getInstance().func_241870_a(text), x, y, -1);
        }
        if (tooltip != null && this.isHovered()) {
            tooltip.render(this, matrixStack, mouseX, mouseY);
        }
    }

    @Override
    public void playDownSound(SoundHandler handler) {
        // no-op
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return active && visible && isHovered;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return active && visible && isHovered;
    }

    public void setTexts(ITextProperties... texts) {
        this.setTexts(Arrays.asList(texts));
    }

    public void setTexts(List<? extends ITextProperties> texts) {
        this.texts = texts;
        this.recalculateBounds();
    }

    public void wrap(int wrapWidth) {
        final List<ITextProperties> wrapped = new ArrayList<>(texts.size());
        final CharacterManager charManager = font.getCharacterManager();
        for (ITextProperties text : texts) {
            if (font.getStringPropertyWidth(text) > wrapWidth) {
                wrapped.addAll(charManager.func_238362_b_(text, wrapWidth, Style.EMPTY));
            } else {
                wrapped.add(text);
            }
        }
        this.setTexts(wrapped);
    }

    public enum AnchorX {
        LEFT,
        RIGHT,
        CENTER
    }

    public enum AnchorY {
        TOP,
        BOTTOM,
        CENTER
    }

    public interface Tooltip {
        void render(LabelWidget label, MatrixStack matrixStack, int mouseX, int mouseY);
    }
}
