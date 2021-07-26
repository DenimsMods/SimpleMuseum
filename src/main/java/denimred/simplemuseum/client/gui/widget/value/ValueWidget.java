package denimred.simplemuseum.client.gui.widget.value;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.gui.screen.PuppetConfigScreen;
import denimred.simplemuseum.client.gui.widget.IconButton;
import denimred.simplemuseum.client.gui.widget.LabelWidget;
import denimred.simplemuseum.client.gui.widget.LabelWidget.AnchorX;
import denimred.simplemuseum.client.gui.widget.LabelWidget.AnchorY;
import denimred.simplemuseum.client.gui.widget.NestedWidget;
import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.client.util.LazyUtil;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;
import denimred.simplemuseum.common.i18n.Descriptive;
import denimred.simplemuseum.common.i18n.I18nUtil;

public abstract class ValueWidget<T, V extends PuppetValue<T, ?>> extends NestedWidget
        implements Descriptive {
    protected static final Minecraft MC = ClientUtil.MC;
    protected static final ResourceLocation BUTTONS_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/value_buttons.png");
    protected static final int HEIGHT_MARGIN = 8;
    protected static final int TITLE_OFFSET = 15;
    public final PuppetConfigScreen parent;
    protected final V value;
    protected final LabelWidget titleLabel;
    protected final IconButton revertButton;
    protected final IconButton resetButton;
    protected final IconButton deleteButton;
    protected final List<IFormattableTextComponent> description = new ArrayList<>();
    protected final List<IFormattableTextComponent> advancedDescription = new ArrayList<>();
    protected final T original;
    protected boolean changed;
    protected int heightMargin = 0;

    public ValueWidget(PuppetConfigScreen parent, int x, int y, int width, int height, V value) {
        super(x, y, width, height, new TranslationTextComponent(value.provider.translationKey));
        this.parent = parent;
        this.value = value;
        this.original = value.get();
        this.prepareText();
        this.titleLabel =
                this.addChild(
                        new LabelWidget(
                                x + width / 2,
                                y,
                                MC.fontRenderer,
                                AnchorX.CENTER,
                                AnchorY.TOP,
                                message));
        titleLabel.setTooltip((lw, ms, mx, my) -> parent.renderWidgetTooltip(this, ms, mx, my));
        revertButton =
                this.addChild(
                        new IconButton(
                                0,
                                0,
                                20,
                                20,
                                BUTTONS_TEXTURE,
                                0,
                                0,
                                64,
                                64,
                                20,
                                this::revert,
                                parent::renderWidgetTooltip,
                                new StringTextComponent("Revert Changes")));
        resetButton =
                this.addChild(
                        new IconButton(
                                0,
                                0,
                                20,
                                20,
                                BUTTONS_TEXTURE,
                                20,
                                0,
                                64,
                                64,
                                20,
                                this::reset,
                                parent::renderWidgetTooltip,
                                new StringTextComponent("Reset To Default")));
        deleteButton =
                this.addChild(
                        new IconButton(
                                0,
                                0,
                                20,
                                20,
                                BUTTONS_TEXTURE,
                                40,
                                0,
                                64,
                                64,
                                20,
                                this::delete,
                                parent::renderWidgetTooltip,
                                new StringTextComponent("Delete/Clear")));
        deleteButton.visible = false;
    }

    @Override
    protected void recalculateChildren() {
        titleLabel.x = x + width / 2;
        titleLabel.y = y + heightMargin;
    }

    protected void prepareText() {
        description.add(
                new TranslationTextComponent(I18nUtil.desc(value.provider.translationKey))
                        .mergeStyle(TextFormatting.GRAY));
        advancedDescription.add(
                new StringTextComponent(value.provider.key.toString())
                        .mergeStyle(TextFormatting.DARK_GRAY));
        advancedDescription.add(
                new StringTextComponent(
                                "- " + LazyUtil.getNbtTagName(value.provider.serializer.getTagId()))
                        .mergeStyle(TextFormatting.DARK_GRAY));
        advancedDescription.add(
                new StringTextComponent("- " + value.provider.serializer.getType().getSimpleName())
                        .mergeStyle(TextFormatting.DARK_GRAY));
    }

    protected void setChangeButtonsPos(int x, int y) {
        revertButton.x = x;
        revertButton.y = y;
        resetButton.x = x + 20;
        resetButton.y = y;
        deleteButton.x = x + 40;
        deleteButton.y = y;
    }

    protected void revert(Button button) {
        value.set(original);
        this.detectAndSync();
    }

    protected void reset(Button button) {
        value.reset();
        this.detectAndSync();
    }

    protected void delete(Button button) {}

    public void detectAndSync() {
        this.detectChanges();
        this.syncWithValue();
    }

    public void detectChanges() {
        changed = this.hasChanged();
        revertButton.active = changed;
        resetButton.active = !this.isDefault();
        deleteButton.active = this.canDelete();
    }

    public void syncWithValue() {}

    protected boolean hasChanged() {
        return !value.get().equals(original);
    }

    protected boolean isDefault() {
        return value.isDefault();
    }

    protected boolean canDelete() {
        return false;
    }

    @Override
    public IFormattableTextComponent getTitle() {
        return (IFormattableTextComponent) message;
    }

    @Override
    public List<IFormattableTextComponent> getDescription() {
        return description;
    }

    @Override
    public List<IFormattableTextComponent> getAdvancedDescription() {
        return advancedDescription;
    }

    @Override
    public boolean hideDescription() {
        return false;
    }
}
