package denimred.simplemuseum.client.gui.screen.behavior;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import denimred.simplemuseum.client.MovementEditorClient;
import denimred.simplemuseum.client.gui.screen.SelectScreen;
import denimred.simplemuseum.client.gui.widget.BetterButton;
import denimred.simplemuseum.common.entity.puppet.goals.MovePuppetGoal;
import denimred.simplemuseum.common.entity.puppet.goals.movement.Movement;

public class PuppetMovementSelectScreen extends SelectScreen<String> {

    public static List<String> movementList = new ArrayList<>();

    private final MovePuppetGoal goal;

    public PuppetMovementSelectScreen(Screen parent, MovePuppetGoal parentGoal) {
        super(parent, new TextComponent("Select Movement"));
        goal = parentGoal;
    }

    @Override
    protected void init() {
        super.init();
        if(parent == null) {
            final int top = (10 * 2) + font.lineHeight;
            addButton(new BetterButton(width - 108, top + font.lineHeight + 26, 100, 20, new TextComponent("Create Path..."), btn -> createMovement(Movement.MoveType.Path)));
            addButton(new BetterButton(width - 108, top + font.lineHeight + 48, 100, 20, new TextComponent("Create Area..."), btn -> createMovement(Movement.MoveType.Area)));
        }
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    protected void onSave() {
//        if(selected != null && goal != null)
//            goal.setMovement(selected.value);
        if(parent == null) {

        }
        //ToDo Repopulate parent
    }

    private void createMovement(Movement.MoveType type) {
        onClose();
        MovementEditorClient.createNewMovement(type);
        Minecraft.getInstance().player.displayClientMessage(new TextComponent("Started creating new Movement").withStyle(ChatFormatting.GREEN), true);
    }

    @Override
    protected boolean isSelected(ListWidget.Entry entry) {
        return false;
    }

    @Override
    protected CompletableFuture<List<String>> getEntriesAsync() {
        return CompletableFuture.completedFuture(movementList);
    }

}
