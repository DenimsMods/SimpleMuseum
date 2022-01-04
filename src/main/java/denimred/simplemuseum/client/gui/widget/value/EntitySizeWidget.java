package denimred.simplemuseum.client.gui.widget.value;

import com.mojang.datafixers.util.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.EntityDimensions;

import java.util.Optional;

import denimred.simplemuseum.client.gui.screen.PuppetConfigScreen;
import denimred.simplemuseum.client.gui.widget.BetterTextFieldWidget;
import denimred.simplemuseum.client.gui.widget.DescriptiveButton;
import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.client.util.NumberUtil;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;
import denimred.simplemuseum.common.entity.puppet.manager.value.standard.EntitySizeValue;

public final class EntitySizeWidget extends ValueWidget<EntityDimensions, EntitySizeValue> {
    private final EditBox widthField;
    private final EditBox heightField;
    private final DescriptiveButton autoCalculateButton;

    public EntitySizeWidget(PuppetConfigScreen parent, PuppetValue<?, ?> value) {
        this(parent, 0, 0, 0, 35 + HEIGHT_MARGIN * 2, value.cast());
        heightMargin = HEIGHT_MARGIN;
    }

    public EntitySizeWidget(
            PuppetConfigScreen parent, int x, int y, int width, int height, EntitySizeValue value) {
        super(parent, x, y, width, height, value);
        autoCalculateButton =
                this.addChild(
                        new DescriptiveButton(
                                0,
                                0,
                                0,
                                20,
                                new TextComponent("Calculate"),
                                new TextComponent(
                                                "Automatically calculate the physical bounds of the puppet using the puppet's model geometry as a reference.")
                                        .withStyle(ChatFormatting.GRAY),
                                this::autoCalculate,
                                parent::renderWidgetTooltip));
        widthField = this.addChild(new SizeHalfFieldWidget(false), true);
        heightField = this.addChild(new SizeHalfFieldWidget(true), true);
        this.setMaxWidth(298);
        this.detectAndSync();
    }

    @Override
    protected void recalculateChildren() {
        final int yPos = y + TITLE_OFFSET + heightMargin;
        final int widgetWidth = (width - 40) / 3;
        final AbstractWidget[] mainChildren = {autoCalculateButton, widthField, heightField};
        for (int i = 0; i < mainChildren.length; i++) {
            final AbstractWidget child = mainChildren[i];
            child.x = x + widgetWidth * i;
            child.y = yPos;
            child.setWidth(widgetWidth);
        }
        this.setChangeButtonsPos(x + widgetWidth * mainChildren.length, yPos);
        super.recalculateChildren();
    }

    private void autoCalculate(Button button) {
        final Optional<EntityDimensions> collisionBounds =
                ClientUtil.getPuppetBounds(valueRef.manager.puppet).map(Pair::getFirst);
        if (collisionBounds.isPresent()) {
            valueRef.set(collisionBounds.get());
            this.syncWithValue();
        }
    }

    @Override
    public void syncWithValue() {
        widthField.setValue(String.valueOf(valueRef.getWidth()));
        widthField.moveCursorToStart();
        heightField.setValue(String.valueOf(valueRef.getHeight()));
        heightField.moveCursorToStart();
    }

    @Override
    protected boolean hasChanged() {
        return !(valueRef.getWidth() == original.width && valueRef.getHeight() == original.height);
    }

    private final class SizeHalfFieldWidget extends BetterTextFieldWidget {
        private final boolean isHeight;

        public SizeHalfFieldWidget(boolean isHeight) {
            super(
                    ClientUtil.MC.font,
                    0,
                    0,
                    0,
                    20,
                    isHeight ? new TextComponent("Height") : new TextComponent("Width"));
            this.isHeight = isHeight;
            this.setFilter(this::validate);
            this.setResponder(this::respond);
        }

        private boolean validate(String s) {
            return NumberUtil.isValidFloat(s, false);
        }

        private void respond(String s) {
            final Optional<Float> oF = NumberUtil.parseFloat(s);
            if (oF.isPresent()) {
                final float f = oF.get();
                if (isHeight) {
                    this.setTextColor(valueRef.testHeight(f) ? TEXT_VALID : TEXT_INVALID);
                    valueRef.setHeight(f);
                } else {
                    this.setTextColor(valueRef.testWidth(f) ? TEXT_VALID : TEXT_INVALID);
                    valueRef.setWidth(f);
                }
            } else {
                this.setTextColor(TEXT_ERROR);
            }
            EntitySizeWidget.this.detectChanges();
        }
    }
}
