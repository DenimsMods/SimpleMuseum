package denimred.simplemuseum.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
            PoseStack poseStack, Font font, AbstractWidget widget, Component text, boolean bright) {
        drawStringLeft(poseStack, font, widget, text, bright ? 0xFFFFFF : 0xA0A0A0);
    }

    protected static void drawStringLeft(
            PoseStack poseStack, Font font, AbstractWidget widget, Component text, int color) {
        drawString(
                poseStack,
                font,
                text,
                widget.x - font.width(text) - MARGIN,
                widget.y + widget.getHeight() / 2 - font.lineHeight / 2,
                color);
    }

    @Override
    public void init() {
        minecraft.keyboardHandler.setSendRepeatsToGui(true);
    }

    @Override
    public void onClose() {
        mc.setScreen(parent);
    }

    @Override
    public void removed() {
        mc.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected void renderWidgetTooltip(
            AbstractWidget widget, PoseStack poseStack, int mouseX, int mouseY) {
        this.renderTooltip(poseStack, widget.getMessage(), mouseX, mouseY);
    }
}
