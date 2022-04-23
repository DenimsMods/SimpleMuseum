package denimred.simplemuseum.client.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import denimred.simplemuseum.client.gui.widget.PuppetGoalWidget;
import denimred.simplemuseum.common.entity.puppet.goals.PuppetGoalTree;

public class PuppetGoalTreeEditorScreen extends Screen {

    private final PuppetConfigScreen parent;
    private final PuppetGoalTree tree;

    public PuppetGoalTreeEditorScreen(PuppetConfigScreen parent, PuppetGoalTree tree) {
        super(new TextComponent("Goal Tree Editor"));
        this.parent = parent;
        this.tree = tree;
    }

    @Override
    protected void init() {

    }

    public void refreshGoals() {
        int i = 0;
        tree.getGoalList().forEach(goal -> {
            //addWidget(new PuppetGoalWidget());
        });
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
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
