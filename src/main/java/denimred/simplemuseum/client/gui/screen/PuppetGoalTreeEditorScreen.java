package denimred.simplemuseum.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import denimred.simplemuseum.client.gui.widget.BetterButton;
import denimred.simplemuseum.common.entity.puppet.goals.PuppetGoalTree;

public class PuppetGoalTreeEditorScreen extends Screen {

    private final PuppetConfigScreen parent;
    private final PuppetGoalTree tree;

    private PuppetGoalList goalList;

    public PuppetGoalTreeEditorScreen(PuppetConfigScreen parent, PuppetGoalTree tree) {
        super(new TextComponent("AI Goal Editor"));
        this.parent = parent;
        this.tree = tree;
    }

    @Override
    protected void init() {
        buttons.clear();
        children.clear();
        goalList = addWidget(new PuppetGoalList(minecraft, 25, height - 25, width / 2));

        addButton(new BetterButton(20, height - 25, 20, 20, new TextComponent("+"), btn -> createGoal()));

        populateList();
    }

    public void populateList() {
        goalList.clear();
        for(int i = 0; i < tree.getGoalList().size(); i++)
            goalList.addGoal(i+1, null);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);
        drawCenteredString(
                poseStack,
                font,
                title.plainCopy().withStyle(ChatFormatting.UNDERLINE),
                width / 2,
                10,
                0xFFFFFF);
        //Mockup UI
        //Right - Goal Editor
        int editorX = (width / 2) + 5;
        fill(poseStack, editorX, 25, width - 20, height - 25, 0x55FFFFFF); //bg1 - Outline
        fill(poseStack, editorX + 1, 26, width - 21, height - 26, 0xc0101010); //bg2
        //Goal Editor Properties based on Goal Type
        fill(poseStack, editorX + 5, height - 50, editorX + 100, height - 30, 0x55999999); //save goal
        fill(poseStack, width - 110, height - 50, width - 25, height - 30, 0x55999999); //delete goal

        this.goalList.render(poseStack, mouseX, mouseY, partialTicks);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private void createGoal() {
        tree.getGoalList().add(null);
        goalList.addGoal(tree.getGoalList().size(), null);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
        //ToDo Remove this once finished testing non-sync
        tree.getGoalList().clear();
    }

    class PuppetGoalList extends ObjectSelectionList<PuppetGoalList.Entry> {
        int listWidth;

        public PuppetGoalList(Minecraft minecraft, int top, int bottom, int width) {
            super(minecraft, width, bottom - top, top, bottom, 20);
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            this.listWidth = width;
        }

        public void addGoal(int priority, Goal.Flag... flags) {
            addEntry(new Entry(priority, flags));
        }

        public void clear() {
            clearEntries();
        }

        public class Entry extends ObjectSelectionList.Entry<PuppetGoalList.Entry> {
            private final int priority;
            private final Goal.Flag[] flags;

            public Entry(int priority, Goal.Flag... flags) {
                this.priority = priority;
                this.flags = flags;
            }

            @Override
            public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
                fill(poseStack, left, top, left + width, top + height, index % 2 == 0 ? 0xFFFFFFFF : 0xFF777777);
                drawString(poseStack, font, ""+priority, left, top + (height / 2), 0xFFc1c1c1);
            }

        }
    }

    class PuppetGoalSelectScreen extends SelectScreen<GoalType> {
        protected PuppetGoalSelectScreen(Screen parent, Component title) {
            super(parent, title);
        }

        @Override
        protected void onSave() {

        }

        @Override
        protected boolean isSelected(SelectScreen<GoalType>.ListWidget.Entry entry) {
            return false;
        }

        @Override
        protected CompletableFuture<List<GoalType>> getEntriesAsync() {
            return null;
        }
    }

    enum GoalType {
        Idle, Wander, Path
    }

}
