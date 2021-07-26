package denimred.simplemuseum.client.gui.widget.value;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import denimred.simplemuseum.client.gui.screen.test.PuppetConfigScreen;
import denimred.simplemuseum.client.gui.screen.test.SelectScreen;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetRenderManager.NameplateBehavior;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

// TODO: Make a proper enum widget
public final class NameplateBehaviorWidget
        extends ValueWidget<NameplateBehavior, PuppetValue<NameplateBehavior, ?>> {
    private final ExtendedButton button;

    public NameplateBehaviorWidget(PuppetConfigScreen parent, PuppetValue<?, ?> value) {
        this(parent, 0, 0, 0, 35 + HEIGHT_MARGIN * 2, value.cast());
        heightMargin = HEIGHT_MARGIN;
    }

    public NameplateBehaviorWidget(
            PuppetConfigScreen parent,
            int x,
            int y,
            int width,
            int height,
            PuppetValue<NameplateBehavior, ?> value) {
        super(parent, x, y, width, height, value);
        button =
                this.addChild(
                        new ExtendedButton(
                                0, 0, 0, 20, StringTextComponent.EMPTY, this::selectEnum));
        this.setMaxWidth(100);
        this.detectAndSync();
    }

    @Override
    protected void recalculateChildren() {
        final int yPos = y + TITLE_OFFSET + heightMargin;
        final int widgetWidth = width - 40;
        button.x = x;
        button.y = yPos;
        button.setWidth(widgetWidth);
        this.setChangeButtonsPos(x + widgetWidth, yPos);
        super.recalculateChildren();
    }

    private void selectEnum(Button button) {
        MC.displayGuiScreen(new AnimSelectScreen());
    }

    @Override
    public void syncWithValue() {
        button.setMessage(new StringTextComponent(value.get().name()));
    }

    private final class AnimSelectScreen extends SelectScreen<NameplateBehavior> {
        protected AnimSelectScreen() {
            super(
                    NameplateBehaviorWidget.this.parent,
                    new StringTextComponent("Select Nameplate Behavior"));
        }

        @Override
        protected void onSave() {
            if (selected != null) {
                value.set(selected.value);
                NameplateBehaviorWidget.this.syncWithValue();
            }
        }

        @Override
        protected boolean isSelected(ListWidget.Entry entry) {
            return entry.value.equals(value.get());
        }

        @Override
        protected CompletableFuture<List<NameplateBehavior>> getEntriesAsync() {
            return CompletableFuture.completedFuture(Arrays.asList(NameplateBehavior.values()));
        }
    }
}
