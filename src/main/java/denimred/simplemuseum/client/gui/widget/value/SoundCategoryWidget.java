package denimred.simplemuseum.client.gui.widget.value;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import denimred.simplemuseum.client.gui.screen.PuppetConfigScreen;
import denimred.simplemuseum.client.gui.screen.SelectScreen;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

// TODO: Make a proper enum widget
public final class SoundCategoryWidget
        extends ValueWidget<SoundCategory, PuppetValue<SoundCategory, ?>> {
    private final ExtendedButton button;

    public SoundCategoryWidget(PuppetConfigScreen parent, PuppetValue<?, ?> value) {
        this(parent, 0, 0, 0, 35 + HEIGHT_MARGIN * 2, value.cast());
        heightMargin = HEIGHT_MARGIN;
    }

    public SoundCategoryWidget(
            PuppetConfigScreen parent,
            int x,
            int y,
            int width,
            int height,
            PuppetValue<SoundCategory, ?> value) {
        super(parent, x, y, width, height, value);
        button =
                this.addChild(
                        new ExtendedButton(
                                0, 0, 0, 20, StringTextComponent.EMPTY, this::selectEnum));
        this.setMaxWidth(120);
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

    private final class AnimSelectScreen extends SelectScreen<SoundCategory> {
        protected AnimSelectScreen() {
            super(
                    SoundCategoryWidget.this.parent,
                    new StringTextComponent("Select Sound Category"));
        }

        @Override
        protected void onSave() {
            if (selected != null) {
                value.set(selected.value);
                SoundCategoryWidget.this.syncWithValue();
            }
        }

        @Override
        protected boolean isSelected(ListWidget.Entry entry) {
            return entry.value.equals(value.get());
        }

        @Override
        protected CompletableFuture<List<SoundCategory>> getEntriesAsync() {
            return CompletableFuture.completedFuture(Arrays.asList(SoundCategory.values()));
        }
    }
}
