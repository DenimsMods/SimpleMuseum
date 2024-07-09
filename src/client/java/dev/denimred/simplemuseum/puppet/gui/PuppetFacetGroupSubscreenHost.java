package dev.denimred.simplemuseum.puppet.gui;

import dev.denimred.simplemuseum.SimpleMuseumClient;
import dev.denimred.simplemuseum.puppet.data.PuppetFacetGroup;
import dev.denimred.simplemuseum.puppet.gui.subscreen.EmptySubscreen;
import dev.denimred.simplemuseum.puppet.gui.subscreen.Subscreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public final class PuppetFacetGroupSubscreenHost extends AbstractWidget {
    private Subscreen subscreen = EmptySubscreen.BLANK;

    public PuppetFacetGroupSubscreenHost(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    public void setSubscreen(PuppetFacetGroup group, String section) {
        var factory = SimpleMuseumClient.getSubscreenFactory(group, section);
        subscreen.removed();
        subscreen = factory.createSubscreen(group, section);
        subscreen.added();
        subscreen.init(Minecraft.getInstance(), width, height);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(getX(), getY(), 0f);
        subscreen.render(guiGraphics, mouseX - getX(), mouseY - getY(), partialTick);
        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {} // This is too low of a priority unfortunately
}
