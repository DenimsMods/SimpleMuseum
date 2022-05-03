package denimred.simplemuseum.client.gui.screen.behavior;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import denimred.simplemuseum.client.gui.screen.SelectScreen;
import denimred.simplemuseum.common.entity.puppet.goals.MovePuppetGoal;
import denimred.simplemuseum.common.entity.puppet.goals.movement.Movement;

public class PuppetMovementSelectScreen extends SelectScreen<Movement> {

    private final MovePuppetGoal goal;

    public PuppetMovementSelectScreen(Screen parent, MovePuppetGoal parentGoal) {
        super(parent, new TextComponent("Select Movement"));
        goal = parentGoal;
    }

    @Override
    protected void onSave() {
        if(selected != null)
            goal.setMovement(selected.value);
        //ToDo Repopulate parent
    }

    @Override
    protected boolean isSelected(ListWidget.Entry entry) {
        return false;
    }

    @Override
    protected CompletableFuture<List<Movement>> getEntriesAsync() {
        return null;
    }

}
