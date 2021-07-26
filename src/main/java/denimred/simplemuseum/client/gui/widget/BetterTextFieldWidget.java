package denimred.simplemuseum.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public class BetterTextFieldWidget extends TextFieldWidget implements ITickingWidget {
    public static final int MAX_PACKET_STRING = 32767;
    public static final int TEXT_VALID = 0xFFFFFF;
    public static final int TEXT_INVALID = 0xFFFF00;
    public static final int TEXT_ERROR = 0xFF0000;
    public static final float DARKNESS_FACTOR = 0.7F;
    public static final int OPAQUE_MASK = 0xFF000000;
    protected int enabledColorDarker;
    protected int disabledColorDarker;

    public BetterTextFieldWidget(
            FontRenderer fontRenderer, int x, int y, int width, int height, ITextComponent title) {
        this(fontRenderer, x, y, width, height, null, title);
    }

    public BetterTextFieldWidget(
            FontRenderer fontRenderer,
            int x,
            int y,
            int width,
            int height,
            @Nullable TextFieldWidget inputWidget,
            ITextComponent title) {
        super(fontRenderer, x, y, width, height, inputWidget, title);
        this.setTextColor(TEXT_VALID);
        this.enabledColor |= OPAQUE_MASK;
        this.disabledColor |= OPAQUE_MASK;
        this.enabledColorDarker = darken(enabledColor);
        this.disabledColorDarker = darken(disabledColor);
    }

    private static int darken(int color) {
        final int a = color >> 24 & 0xFF;
        final int r = (int) ((color >> 16 & 0xFF) * DARKNESS_FACTOR);
        final int g = (int) ((color >> 8 & 0xFF) * DARKNESS_FACTOR);
        final int b = (int) ((color & 0xFF) * DARKNESS_FACTOR);
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        return true;
    }

    @Override
    public void setTextColor(int color) {
        final int opaque = color | OPAQUE_MASK;
        super.setTextColor(opaque);
        this.enabledColorDarker = darken(opaque);
    }

    @Override
    public void setDisabledTextColour(int color) {
        final int opaque = color | OPAQUE_MASK;
        super.setDisabledTextColour(opaque);
        this.disabledColorDarker = darken(opaque);
    }

    @Override
    public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.getVisible()) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            final int colorLight = this.isEnabled ? this.enabledColor : this.disabledColor;
            final int colorDark =
                    this.isEnabled ? this.enabledColorDarker : this.disabledColorDarker;
            final boolean drawBackground = this.getEnableBackgroundDrawing();
            if (drawBackground) {
                fill(
                        matrixStack,
                        this.x,
                        this.y,
                        this.x + this.width,
                        this.y + this.height,
                        this.isFocused() ? colorLight : colorDark);
                fill(
                        matrixStack,
                        this.x + 1,
                        this.y + 1,
                        this.x + this.width - 1,
                        this.y + this.height - 1,
                        0xDD000000);
            }

            int j = this.cursorPosition - this.lineScrollOffset;
            int k = this.selectionEnd - this.lineScrollOffset;
            String s =
                    this.fontRenderer.trimStringToWidth(
                            this.text.substring(this.lineScrollOffset), this.getAdjustedWidth());
            boolean flag = j >= 0 && j <= s.length();
            boolean showCursor = this.isFocused() && this.cursorCounter / 6 % 2 == 0 && flag;
            int l = drawBackground ? this.x + 4 : this.x;
            int i1 = drawBackground ? this.y + (this.height - 8) / 2 : this.y;
            int j1 = l;
            if (k > s.length()) {
                k = s.length();
            }

            if (!s.isEmpty()) {
                String s1 = flag ? s.substring(0, j) : s;
                j1 =
                        this.fontRenderer.drawTextWithShadow(
                                matrixStack,
                                this.textFormatter.apply(s1, this.lineScrollOffset),
                                (float) l,
                                (float) i1,
                                colorLight);
            }

            boolean flag2 =
                    this.cursorPosition < this.text.length()
                            || this.text.length() >= this.getMaxStringLength();
            int k1 = j1;
            if (!flag) {
                k1 = j > 0 ? l + this.width : l;
            } else if (flag2) {
                k1 = j1 - 1;
                --j1;
            }

            if (!s.isEmpty() && flag && j < s.length()) {
                this.fontRenderer.drawTextWithShadow(
                        matrixStack,
                        this.textFormatter.apply(s.substring(j), this.cursorPosition),
                        (float) j1,
                        (float) i1,
                        colorLight);
            }

            if (!flag2 && this.suggestion != null) {
                this.fontRenderer.drawStringWithShadow(
                        matrixStack, this.suggestion, (float) (k1 - 1), (float) i1, 0xff808080);
            }

            if (showCursor) {
                if (flag2) {
                    AbstractGui.fill(matrixStack, k1, i1 - 1, k1 + 1, i1 + 1 + 9, 0xffd0d0d0);
                } else {
                    this.fontRenderer.drawStringWithShadow(
                            matrixStack, "_", (float) k1, (float) i1, colorLight);
                }
            }

            if (k != j) {
                int l1 = l + this.fontRenderer.getStringWidth(s.substring(0, k));
                this.drawSelectionBox(k1, i1 - 1, l1 - 1, i1 + 1 + 9);
            }
            RenderSystem.disableBlend();
        }
    }
}
