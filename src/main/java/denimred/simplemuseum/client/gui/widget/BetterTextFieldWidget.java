package denimred.simplemuseum.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class BetterTextFieldWidget extends EditBox implements ITickingWidget {
    public static final int MAX_PACKET_STRING = 32767;
    public static final int TEXT_VALID = 0xFFFFFF;
    public static final int TEXT_INVALID = 0xFFFF00;
    public static final int TEXT_ERROR = 0xFF0000;
    public static final float DARKNESS_FACTOR = 0.7F;
    public static final int OPAQUE_MASK = 0xFF000000;
    protected int enabledColorDarker;
    protected int disabledColorDarker;

    public BetterTextFieldWidget(Font font, int x, int y, int width, int height, Component title) {
        this(font, x, y, width, height, null, title);
    }

    public BetterTextFieldWidget(
            Font font,
            int x,
            int y,
            int width,
            int height,
            @Nullable EditBox inputWidget,
            Component title) {
        super(font, x, y, width, height, inputWidget, title);
        this.setTextColor(TEXT_VALID);
        this.textColor |= OPAQUE_MASK;
        this.textColorUneditable |= OPAQUE_MASK;
        this.enabledColorDarker = darken(textColor);
        this.disabledColorDarker = darken(textColorUneditable);
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
    public void setTextColorUneditable(int color) {
        final int opaque = color | OPAQUE_MASK;
        super.setTextColorUneditable(opaque);
        this.disabledColorDarker = darken(opaque);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if (this.isVisible()) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            final int colorLight = this.isEditable ? this.textColor : this.textColorUneditable;
            final int colorDark =
                    this.isEditable ? this.enabledColorDarker : this.disabledColorDarker;
            final boolean drawBackground = this.isBordered();
            if (drawBackground) {
                fill(
                        poseStack,
                        this.x,
                        this.y,
                        this.x + this.width,
                        this.y + this.height,
                        this.isFocused() ? colorLight : colorDark);
                fill(
                        poseStack,
                        this.x + 1,
                        this.y + 1,
                        this.x + this.width - 1,
                        this.y + this.height - 1,
                        0xDD000000);
            }

            int j = this.cursorPos - this.displayPos;
            int k = this.highlightPos - this.displayPos;
            String s =
                    this.font.plainSubstrByWidth(
                            this.value.substring(this.displayPos), this.getInnerWidth());
            boolean flag = j >= 0 && j <= s.length();
            boolean showCursor = this.isFocused() && this.frame / 6 % 2 == 0 && flag;
            int l = drawBackground ? this.x + 4 : this.x;
            int i1 = drawBackground ? this.y + (this.height - 8) / 2 : this.y;
            int j1 = l;
            if (k > s.length()) {
                k = s.length();
            }

            if (!s.isEmpty()) {
                String s1 = flag ? s.substring(0, j) : s;
                j1 =
                        this.font.drawShadow(
                                poseStack,
                                this.formatter.apply(s1, this.displayPos),
                                (float) l,
                                (float) i1,
                                colorLight);
            }

            boolean flag2 =
                    this.cursorPos < this.value.length()
                            || this.value.length() >= this.getMaxLength();
            int k1 = j1;
            if (!flag) {
                k1 = j > 0 ? l + this.width : l;
            } else if (flag2) {
                k1 = j1 - 1;
                --j1;
            }

            if (!s.isEmpty() && flag && j < s.length()) {
                this.font.drawShadow(
                        poseStack,
                        this.formatter.apply(s.substring(j), this.cursorPos),
                        (float) j1,
                        (float) i1,
                        colorLight);
            }

            if (!flag2 && this.suggestion != null) {
                this.font.drawShadow(
                        poseStack, this.suggestion, (float) (k1 - 1), (float) i1, 0xff808080);
            }

            if (showCursor) {
                if (flag2) {
                    GuiComponent.fill(poseStack, k1, i1 - 1, k1 + 1, i1 + 1 + 9, 0xffd0d0d0);
                } else {
                    this.font.drawShadow(poseStack, "_", (float) k1, (float) i1, colorLight);
                }
            }

            if (k != j) {
                int l1 = l + this.font.width(s.substring(0, k));
                this.renderHighlight(k1, i1 - 1, l1 - 1, i1 + 1 + 9);
            }
            RenderSystem.disableBlend();
        }
    }
}
