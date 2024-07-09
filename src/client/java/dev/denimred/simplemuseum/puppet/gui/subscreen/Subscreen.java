package dev.denimred.simplemuseum.puppet.gui.subscreen;

import dev.denimred.simplemuseum.puppet.data.PuppetFacetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public abstract class Subscreen extends Screen {
    public final PuppetFacetGroup group;
    public final String section;

    protected Subscreen(PuppetFacetGroup group, String section) {
        super(group.getDisplayName(section));
        this.group = group;
        this.section = section;
    }

    @Override
    public void onClose() {}

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {}
}
