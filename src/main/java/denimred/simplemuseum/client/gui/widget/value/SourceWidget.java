package denimred.simplemuseum.client.gui.widget.value;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.gui.screen.PuppetConfigScreen;
import denimred.simplemuseum.client.gui.screen.SelectScreen;
import denimred.simplemuseum.client.gui.widget.BetterTextFieldWidget;
import denimred.simplemuseum.client.gui.widget.IconButton;
import denimred.simplemuseum.client.util.ResourceUtil;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;
import denimred.simplemuseum.common.entity.puppet.manager.value.checked.CheckedValue;

public final class SourceWidget
        extends ValueWidget<ResourceLocation, CheckedValue<ResourceLocation>> {
    public static final ResourceLocation FOLDER_BUTTON_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/folder_button.png");
    private final TranslationTextComponent title;
    private final IconButton selectButton;
    private final SourceTextField animField;
    private final String pathPrefix;

    public SourceWidget(PuppetConfigScreen parent, PuppetValue<?, ?> value) {
        this(parent, 0, 0, 0, 35 + HEIGHT_MARGIN * 2, value.cast());
        heightMargin = HEIGHT_MARGIN;
    }

    public SourceWidget(
            PuppetConfigScreen parent,
            int x,
            int y,
            int width,
            int height,
            CheckedValue<ResourceLocation> value) {
        super(parent, x, y, width, height, value);
        final String path = value.provider.defaultValue.getPath();
        pathPrefix = path.substring(0, path.indexOf('/'));
        title = new TranslationTextComponent(value.provider.translationKey);
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
                                this::selectSource));
        animField = this.addChild(new SourceTextField(), true);
        this.setMaxWidth(298);
        this.detectAndSync();
    }

    @Override
    protected void recalculateChildren() {
        final int yPos = y + TITLE_OFFSET + heightMargin;
        selectButton.x = x;
        selectButton.y = yPos;
        animField.x = x + 20;
        animField.y = yPos;
        animField.setWidth(width - 60);
        this.setChangeButtonsPos(animField.x + animField.getWidth(), yPos);
        super.recalculateChildren();
    }

    @Override
    public void syncWithValue() {
        animField.setText(value.get().toString());
        animField.setCursorPositionZero();
    }

    private void selectSource(Button button) {
        MC.displayGuiScreen(new SourceSelectScreen());
    }

    private final class SourceTextField extends BetterTextFieldWidget {
        public SourceTextField() {
            super(MC.fontRenderer, 0, 0, 0, 20, title);
            this.setMaxStringLength(MAX_PACKET_STRING);
            this.setValidator(s -> ResourceLocation.tryCreate(s) != null);
            this.setResponder(this::respond);
        }

        private void respond(String s) {
            try {
                value.set(new ResourceLocation(s));
                if (value.isValid()) {
                    this.setTextColor(TEXT_VALID);
                } else {
                    this.setTextColor(TEXT_INVALID);
                }
            } catch (ResourceLocationException e) {
                this.setTextColor(TEXT_ERROR);
            }
            SourceWidget.this.detectChanges();
        }
    }

    private final class SourceSelectScreen extends SelectScreen<ResourceLocation> {
        protected SourceSelectScreen() {
            super(SourceWidget.this.parent, new StringTextComponent("Select Source File"));
        }

        @Override
        protected void onSave() {
            if (selected != null) {
                value.set(selected.value);
                SourceWidget.this.syncWithValue();
            }
        }

        @Override
        protected boolean isSelected(ListWidget.Entry entry) {
            return entry.value.equals(value.get());
        }

        @Override
        protected CompletableFuture<List<ResourceLocation>> getEntriesAsync() {
            return ResourceUtil.getCachedResourcesAsync(pathPrefix, value);
        }
    }
}
