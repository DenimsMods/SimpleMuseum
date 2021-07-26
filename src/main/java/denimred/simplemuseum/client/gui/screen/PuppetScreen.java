package denimred.simplemuseum.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;

@Deprecated // Boy I sure do love inheritance :)
public abstract class PuppetScreen extends Screen {
    protected static final int MARGIN = 4;
    protected final Minecraft mc = Minecraft.getInstance(); // Parent's is nullable for some reason
    protected final PuppetEntity puppet;
    @Nullable protected final Screen parent;

    protected PuppetScreen(PuppetEntity puppet, @Nullable Screen parent) {
        super(puppet.getDisplayName());
        this.puppet = puppet;
        this.parent = parent;
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
