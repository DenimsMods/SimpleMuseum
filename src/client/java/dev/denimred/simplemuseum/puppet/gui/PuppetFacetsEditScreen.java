package dev.denimred.simplemuseum.puppet.gui;

import dev.denimred.simplemuseum.puppet.edit.OpenPuppetFacetsEditScreen;
import dev.denimred.simplemuseum.puppet.edit.PuppetFacetsEditMenu;
import dev.denimred.simplemuseum.puppet.entity.Puppet;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

@SuppressWarnings("NotNullFieldNotInitialized") // Widgets are initialized late
public final class PuppetFacetsEditScreen extends AbstractContainerScreen<PuppetFacetsEditMenu> {
    private PuppetFacetGroupSubscreenHost subscreenHost;
    private PuppetFacetGroupsSidebar sidebar;

    private PuppetFacetsEditScreen(PuppetFacetsEditMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    public static void open(OpenPuppetFacetsEditScreen packet, LocalPlayer player) {
        var puppet = Puppet.getPuppet(player.clientLevel, packet.puppetId, "open puppet facets edit screen", EnvType.SERVER);
        var menu = new PuppetFacetsEditMenu(packet.containerId, puppet);
        var screen = new PuppetFacetsEditScreen(menu, player.getInventory(), puppet.getDisplayName());
        player.containerMenu = menu;
        Minecraft.getInstance().setScreen(screen);
    }

    @Override
    protected void init() {
        leftPos = topPos = 0;
        subscreenHost = addRenderableWidget(new PuppetFacetGroupSubscreenHost(96, 0, width - 96, height));
        sidebar = addRenderableWidget(new PuppetFacetGroupsSidebar(0, 0, 96, height, font, subscreenHost::setSubscreen));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {} // Unused

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {} // Unused

    @Override
    protected void rebuildWidgets() {
        var selectedGroup = sidebar.getSelectedGroup();
        var selectedSection = sidebar.getSelectedSection();
        super.rebuildWidgets();
        sidebar.setSelections(selectedGroup, selectedSection);
    }
}
