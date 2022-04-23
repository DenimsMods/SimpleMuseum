package denimred.simplemuseum.client.gui.widget.value;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

import denimred.simplemuseum.client.gui.screen.PuppetGoalTreeEditorScreen;
import denimred.simplemuseum.client.gui.screen.PuppetConfigScreen;
import denimred.simplemuseum.client.gui.widget.BetterButton;
import denimred.simplemuseum.common.entity.puppet.goals.PuppetGoalTree;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

public final class PuppetGoalTreeWidget extends ValueWidget<PuppetGoalTree, PuppetValue<PuppetGoalTree, ?>> {
    private final BetterButton button;

    public PuppetGoalTreeWidget(PuppetConfigScreen parent, PuppetValue<?,?> value) {
        this(parent, 0, 0, 0, 35 + HEIGHT_MARGIN * 2, value.cast());
        heightMargin = HEIGHT_MARGIN;
    }

    public PuppetGoalTreeWidget(PuppetConfigScreen parent, int x, int y, int width, int height, PuppetValue<PuppetGoalTree, ?> valueRef) {
        super(parent, x, y, width, height, valueRef);
        button = this.addChild(new BetterButton(0, 0, 100, 20, new TextComponent("Edit"), (btn) -> {
            Minecraft.getInstance().setScreen(new PuppetGoalTreeEditorScreen(parent, valueRef.get()));
        }));
    }

    @Override
    protected void recalculateChildren() {
        final int yPos = y + TITLE_OFFSET + heightMargin;
        final int widgetWidth = width - 100;
        button.x = x + 50;
        button.y = yPos;
        button.setWidth(widgetWidth);
        super.recalculateChildren();
    }
}
