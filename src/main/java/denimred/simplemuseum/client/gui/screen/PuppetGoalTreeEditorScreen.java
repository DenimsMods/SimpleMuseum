package denimred.simplemuseum.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import denimred.simplemuseum.client.gui.widget.BetterButton;
import denimred.simplemuseum.client.gui.widget.LabelWidget;
import denimred.simplemuseum.client.gui.widget.NestedWidget;
import denimred.simplemuseum.common.entity.puppet.goals.PuppetGoalTree;

public class PuppetGoalTreeEditorScreen extends Screen {

    private final PuppetConfigScreen parent;
    private final PuppetGoalTree tree;

    public PuppetGoalTreeEditorScreen(PuppetConfigScreen parent, PuppetGoalTree tree) {
        super(new TextComponent("AI Goal Editor"));
        this.parent = parent;
        this.tree = tree;
    }

    @Override
    protected void init() {
        populateWidgets();
    }

    public void populateWidgets() {
        buttons.clear();
        children.clear();

        addButton(new BetterButton(20, height - 25, 20, 20, new TextComponent("+"), btn -> {
            createGoal();
        }));

        for(int i = 0; i < tree.getGoalList().size(); i++)
            addGoalWidget(25 + (tree.getGoalList().size()-1) * 55);
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
        //Left - Goal List
        fill(poseStack, 20, 25, (width / 2) - 5, height - 25, 0xc0101010);
        fill(poseStack, (width / 2) - 10, 25, (width / 2) - 5, height - 25, 0xFF777777);
        //Right - Goal Editor
        int editorX = (width / 2) + 5;
        fill(poseStack, editorX, 25, width - 20, height - 25, 0x55FFFFFF); //bg1 - Outline
        fill(poseStack, editorX + 1, 26, width - 21, height - 26, 0xc0101010); //bg2
        //Goal Editor Properties based on Goal Type
        fill(poseStack, editorX + 5, height - 50, editorX + 100, height - 30, 0x55999999); //save goal
        fill(poseStack, width - 110, height - 50, width - 25, height - 30, 0x55999999); //delete goal

        children.forEach(widget -> {
            if(widget instanceof Widget)
                ((Widget) widget).render(poseStack, mouseX, mouseY, partialTicks);
        });
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private void createGoal() {
        tree.getGoalList().add(null);
        addGoalWidget(25 + (tree.getGoalList().size()-1) * 55);
    }

    private void addGoalWidget(int y) {
        addWidget(new PuppetGoalWidget(20, y, (width / 2) - 35, 50, new TextComponent("Widget")));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
        //ToDo Remove this once finished testing non-sync
        tree.getGoalList().clear();
    }

    class PuppetGoalWidget extends NestedWidget {
        private GoalType goalType;

        public PuppetGoalWidget(int x, int y, int width, int height, Component title) {
            this(x, y, width, height, title, GoalType.Idle);
        }

        public PuppetGoalWidget(int x, int y, int width, int height, Component title, GoalType goalType) {
            super(x, y, width, height, title);
            this.goalType = goalType;
        }

        @Override
        public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            fill(poseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0x55FFFFFF);
            fill(poseStack, this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, 0xc0101010);
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
