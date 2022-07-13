package denimred.simplemuseum.client.gui.widget.value;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.gui.screen.PuppetConfigScreen;
import denimred.simplemuseum.client.gui.screen.SelectScreen;
import denimred.simplemuseum.client.gui.widget.BetterTextFieldWidget;
import denimred.simplemuseum.client.gui.widget.IconButton;
import denimred.simplemuseum.client.resources.data.ExpressionDataSection;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetAnimationManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;
import denimred.simplemuseum.common.entity.puppet.manager.value.checked.CheckedValue;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class ExpressionWidget extends ValueWidget<String, CheckedValue<String>> {
    public static final ResourceLocation FOLDER_BUTTON_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/folder_button.png");
    private final TranslatableComponent title;
    private final IconButton selectButton;
    private final ExpressionTextField expressionField;

    public ExpressionWidget(PuppetConfigScreen parent, PuppetValue<?, ?> value) {
        this(parent, 0, 0, 0, 35 + HEIGHT_MARGIN * 2, value.cast());
        heightMargin = HEIGHT_MARGIN;
    }

    public ExpressionWidget(
            PuppetConfigScreen parent,
            int x,
            int y,
            int width,
            int height,
            CheckedValue<String> value) {
        super(parent, x, y, width, height, value);
        title = new TranslatableComponent(value.provider.translationKey);
        selectButton =
                this.addChild(
                        new IconButton(
                                0,
                                0,
                                20,
                                20,
                                FOLDER_BUTTON_TEXTURE,
                                0,
                                0,
                                64,
                                32,
                                20,
                                this::selectExpression));
        expressionField = this.addChild(new ExpressionTextField(), true);
        this.setMaxWidth(200);
        this.detectAndSync();
    }

    @Override
    protected void recalculateChildren() {
        final int yPos = y + TITLE_OFFSET + heightMargin;
        selectButton.x = x;
        selectButton.y = yPos;
        expressionField.x = x + 20;
        expressionField.y = yPos;
        expressionField.setWidth(width - 60);
        this.setChangeButtonsPos(expressionField.x + expressionField.getWidth(), yPos);
        super.recalculateChildren();
    }

    @Override
    public void detectChanges() {
        super.detectChanges();
        final ResourceLocation expressionLoc = parent.getModelFile();
        final ExpressionDataSection dataSection = PuppetAnimationManager.getExpressionData(expressionLoc);
        final String expression = expressionField.getValue();
        final boolean fileExists = dataSection != null;
        if (!fileExists || expression.isEmpty() || dataSection.hasExpression(expression)) {
            expressionField.setTextColor(BetterTextFieldWidget.TEXT_VALID);
        } else {
            expressionField.setTextColor(BetterTextFieldWidget.TEXT_INVALID);
        }
        selectButton.active = fileExists;
    }

    @Override
    public void syncWithValue() {
        expressionField.setValue(valueRef.get());
        expressionField.moveCursorToStart();
    }

    private void selectExpression(Button button) {
        MC.setScreen(new ExpressionSelectScreen());
    }

    private final class ExpressionTextField extends BetterTextFieldWidget {
        public ExpressionTextField() {
            super(MC.font, 0, 0, 0, 20, title);
            this.setMaxLength(MAX_PACKET_STRING);
            this.setResponder(this::respond);
        }

        private void respond(String s) {
            valueRef.set(s);
            ExpressionWidget.this.detectChanges();
        }
    }

    private final class ExpressionSelectScreen extends SelectScreen<String> {
        private ExpressionSelectScreen() {
            super(ExpressionWidget.this.parent, new TextComponent("Select Expression"));
        }

        @Override
        protected void onSave() {
            if (selected != null) {
                valueRef.set(selected.value);
                ExpressionWidget.this.syncWithValue();
            }
        }

        @Override
        protected boolean isSelected(ListWidget.Entry entry) {
            return entry.value.equals(valueRef.get());
        }

        @Override
        protected CompletableFuture<List<String>> getEntriesAsync() {
            final ResourceLocation loc = ExpressionWidget.this.parent.getModelFile();
            final ExpressionDataSection file = PuppetAnimationManager.getExpressionData(loc);
            if (file != null) {
                List<String> list = file.getExpressionList().stream().map(expression -> expression.name).sorted().collect(Collectors.toList());

                return CompletableFuture.supplyAsync(() -> list);
            }
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }
}
