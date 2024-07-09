package dev.denimred.simplemuseum.puppet.gui.subscreen;

import dev.denimred.simplemuseum.puppet.data.PuppetFacetGroup;
import net.minecraft.client.gui.GuiGraphics;

public class DefaultSubscreen extends Subscreen {
    public DefaultSubscreen(PuppetFacetGroup group, String section) {
        super(group, section);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(font, group.getDisplayName(section), width / 2, height / 2 - font.lineHeight / 2, 0xFF999999);
    }
}
