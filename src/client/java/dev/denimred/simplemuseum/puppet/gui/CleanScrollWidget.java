package dev.denimred.simplemuseum.puppet.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.network.chat.Component;

public abstract class CleanScrollWidget extends AbstractScrollWidget {
    public CleanScrollWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;
        this.renderBackground(guiGraphics);
        guiGraphics.enableScissor(getX(), getY(), getX() + width, getY() + height);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0, -scrollAmount(), 0.0);
        this.renderContents(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();
        this.renderDecorations(guiGraphics);
    }

    @Override
    protected int innerPadding() {
        return 0;
    }

    @Override
    protected int getMaxScrollAmount() {
        return Math.max(0, getContentHeight() - (height - innerPadding()));
    }

    @Override
    protected int getContentHeight() {
        return getInnerHeight() + innerPadding();
    }

    @Override
    protected void renderBorder(GuiGraphics guiGraphics, int x, int y, int width, int height) {}

    @Override
    protected double scrollRate() {
        return 10;
    }
}
