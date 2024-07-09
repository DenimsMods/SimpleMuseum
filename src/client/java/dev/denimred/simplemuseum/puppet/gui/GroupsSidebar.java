package dev.denimred.simplemuseum.puppet.gui;

import com.google.common.collect.ImmutableList;
import dev.denimred.simplemuseum.init.SMPuppetFacetGroups;
import dev.denimred.simplemuseum.puppet.data.PuppetFacetGroup;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;

import static dev.denimred.simplemuseum.puppet.gui.PuppetGuiUtils.hLineGradient;
import static dev.denimred.simplemuseum.puppet.gui.PuppetGuiUtils.vLineGradient;

public final class GroupsSidebar extends CleanScrollWidget {
    private final ImmutableList<PuppetFacetGroup> groups;
    private final Font font;
    private final BiConsumer<PuppetFacetGroup, String> selectionCallback;
    private PuppetFacetGroup selectedGroup = SMPuppetFacetGroups.BLANK;
    private String selectedSection = PuppetFacetGroup.ROOT_SECTION;

    public GroupsSidebar(int x, int y, int width, int height, Font font, BiConsumer<PuppetFacetGroup, String> selectionCallback) {
        super(x, y, width, height, Component.empty());
        this.font = font;
        this.selectionCallback = selectionCallback;
        this.groups = SMPuppetFacetGroups.REGISTRY.stream().filter(PuppetFacetGroup.NOT_BLANK).sorted().collect(ImmutableList.toImmutableList());
        if (!groups.isEmpty()) setSelectedGroup(groups.get(0));
    }

    public PuppetFacetGroup getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(PuppetFacetGroup group) {
        setSelections(group, group.getFirstSectionId());
    }

    public void setSelections(PuppetFacetGroup group, String section) {
        selectedGroup = group;
        selectedSection = section;
        selectionCallback.accept(group, section);
    }

    public String getSelectedSection() {
        return selectedSection;
    }

    public void setSelectedSection(String sectionId) {
        setSelections(selectedGroup, sectionId);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {} // This is too low of a priority unfortunately

    @Override
    protected boolean scrollbarVisible() {
        return false;
    }

    @Override
    protected int getInnerHeight() {
        return height;
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(getX(), getY(), 0.0);

        var selectedGroupY = 0;
        var selectedGroupHeight = 0;

        for (int i = 0, size = groups.size(); i < size; i++) {
            var group = groups.get(i);
            var groupName = group.getDisplayName();
            var groupNameWidth = font.width(groupName) + 25;
            var groupNameHeight = 20;
            var groupNameX = width - groupNameWidth - 1;
            var groupNameY = 64;

            guiGraphics.renderFakeItem(group.getIcon(), groupNameX + 2, groupNameY + 2);
            guiGraphics.drawString(font, groupName, groupNameX + 20, groupNameY + 6, -1);

            if (group == selectedGroup) {
                selectedGroupY = groupNameY;
                selectedGroupHeight = groupNameHeight;
            }
        }

        vLineGradient(guiGraphics, width - 1, 0, selectedGroupY, 0x00AAAAAA, 0xFFAAAAAA);
        vLineGradient(guiGraphics, width - 1, selectedGroupY + selectedGroupHeight - 1, Math.max(height, getInnerHeight()), 0xFFAAAAAA, 0x00AAAAAA);
        hLineGradient(guiGraphics, width / 2, width - 1, selectedGroupY, 0x00AAAAAA, 0xFFAAAAAA);
        hLineGradient(guiGraphics, width / 2, width - 1, selectedGroupY + selectedGroupHeight - 1, 0x00AAAAAA, 0xFFAAAAAA);

        guiGraphics.pose().popPose();
    }
}
