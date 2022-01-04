package denimred.simplemuseum.client.gui.widget.value;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import denimred.simplemuseum.client.gui.screen.PuppetConfigScreen;
import denimred.simplemuseum.client.gui.widget.BetterTextFieldWidget;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;
import denimred.simplemuseum.common.entity.puppet.manager.value.vanilla.CustomNameValue;

public final class CustomNameWidget extends ValueWidget<Component, CustomNameValue> {
    private final CustomNameField nameField;

    public CustomNameWidget(PuppetConfigScreen parent, PuppetValue<?, ?> value) {
        this(parent, 0, 0, 0, 35 + HEIGHT_MARGIN * 2, value.cast());
        heightMargin = HEIGHT_MARGIN;
    }

    public CustomNameWidget(
            PuppetConfigScreen parent, int x, int y, int width, int height, CustomNameValue value) {
        super(parent, x, y, width, height, value);
        nameField = this.addChild(new CustomNameField(), true);
        this.setMaxWidth(200);
        this.detectAndSync();
    }

    @Override
    protected void recalculateChildren() {
        final int yPos = y + TITLE_OFFSET + heightMargin;
        nameField.x = x;
        nameField.y = yPos;
        nameField.setWidth(width - 40);
        this.setChangeButtonsPos(nameField.x + nameField.getWidth(), yPos);
        super.recalculateChildren();
    }

    @Override
    public void syncWithValue() {
        nameField.setValue(valueRef.get().getContents());
        nameField.moveCursorToStart();
    }

    private final class CustomNameField extends BetterTextFieldWidget {
        public CustomNameField() {
            super(MC.font, 0, 0, 0, 20, CustomNameWidget.this.message);
            this.setMaxLength(MAX_PACKET_STRING);
            this.setResponder(this::respond);
        }

        private void respond(String s) {
            valueRef.set(new TextComponent(s).setStyle(valueRef.get().getStyle()));
            CustomNameWidget.this.detectChanges();
        }
    }
}
