package denimred.simplemuseum.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

import denimred.simplemuseum.common.entity.MuseumDummyEntity;

// Boy I sure do love inheritance :)
public abstract class MuseumDummyScreen extends Screen {
    protected static final int MARGIN = 4;
    protected static final int TEXT_VALID = 0xe0e0e0;
    protected static final int TEXT_INVALID = 0xffff00;
    protected static final int TEXT_ERROR = 0xff0000;
    protected final Minecraft mc = Minecraft.getInstance(); // Parent's is nullable for some reason
    protected final MuseumDummyEntity dummy;
    @Nullable protected final Screen parent;

    protected MuseumDummyScreen(MuseumDummyEntity dummy, @Nullable Screen parent) {
        super(dummy.getDisplayName());
        this.dummy = dummy;
        this.parent = parent;
    }

    protected static void drawStringLeft(
            MatrixStack matrixStack, FontRenderer font, Widget widget, ITextComponent text) {
        drawStringLeft(matrixStack, font, widget, text, 0xA0A0A0);
    }

    protected static void drawStringLeft(
            MatrixStack matrixStack,
            FontRenderer font,
            Widget widget,
            ITextComponent text,
            boolean bright) {
        drawStringLeft(matrixStack, font, widget, text, bright ? 0xFFFFFF : 0xA0A0A0);
    }

    protected static void drawStringLeft(
            MatrixStack matrixStack,
            FontRenderer font,
            Widget widget,
            ITextComponent text,
            int color) {
        drawString(
                matrixStack,
                font,
                text,
                widget.x - font.getStringPropertyWidth(text) - MARGIN,
                widget.y + widget.getHeight() / 2 - font.FONT_HEIGHT / 2,
                color);
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        minecraft.keyboardListener.enableRepeatEvents(true);
        super.init(minecraft, width, height);
    }

    @Override
    public void closeScreen() {
        mc.displayGuiScreen(parent);
    }

    @Override
    public void onClose() {
        mc.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected void renderWidgetTooltip(
            Widget widget, MatrixStack matrixStack, int mouseX, int mouseY) {
        this.renderTooltip(matrixStack, widget.getMessage(), mouseX, mouseY);
    }
}
