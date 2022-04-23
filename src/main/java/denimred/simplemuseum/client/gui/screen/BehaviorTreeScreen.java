package denimred.simplemuseum.client.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

import denimred.simplemuseum.common.entity.puppet.goals.PuppetGoalTree;

public class BehaviorTreeScreen extends Screen {

    private final PuppetConfigScreen parent;
    private final PuppetGoalTree tree;

    public BehaviorTreeScreen(PuppetConfigScreen parent, PuppetGoalTree tree) {
        super(new TextComponent("Behavior Tree Editor"));
        this.parent = parent;
        this.tree = tree;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

}
