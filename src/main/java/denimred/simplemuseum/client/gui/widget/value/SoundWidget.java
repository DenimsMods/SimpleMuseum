package denimred.simplemuseum.client.gui.widget.value;

import net.minecraft.ResourceLocationException;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.gui.screen.PuppetConfigScreen;
import denimred.simplemuseum.client.gui.screen.SelectScreen;
import denimred.simplemuseum.client.gui.widget.BetterTextFieldWidget;
import denimred.simplemuseum.client.gui.widget.IconButton;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;
import denimred.simplemuseum.common.entity.puppet.manager.value.checked.CheckedValue;

public final class SoundWidget
        extends ValueWidget<ResourceLocation, CheckedValue<ResourceLocation>> {
    public static final ResourceLocation FOLDER_BUTTON_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/folder_button.png");
    private final TranslatableComponent title;
    private final IconButton selectButton;
    private final SoundTextField animField;

    public SoundWidget(PuppetConfigScreen parent, PuppetValue<?, ?> value) {
        this(parent, 0, 0, 0, 35 + HEIGHT_MARGIN * 2, value.cast());
        heightMargin = HEIGHT_MARGIN;
    }

    public SoundWidget(
            PuppetConfigScreen parent,
            int x,
            int y,
            int width,
            int height,
            CheckedValue<ResourceLocation> value) {
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
                                this::selectSource));
        animField = this.addChild(new SoundTextField(), true);
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
        animField.setValue(valueRef.get().toString());
        animField.moveCursorToStart();
    }

    private void selectSource(Button button) {
        MC.setScreen(new SoundSelectScreen());
    }

    private final class SoundTextField extends BetterTextFieldWidget {
        public SoundTextField() {
            super(MC.font, 0, 0, 0, 20, title);
            this.setMaxLength(MAX_PACKET_STRING);
            this.setFilter(s -> ResourceLocation.tryParse(s) != null);
            this.setResponder(this::respond);
        }

        private void respond(String s) {
            try {
                valueRef.set(new ResourceLocation(s));
                if (valueRef.isValid()) {
                    this.setTextColor(TEXT_VALID);
                } else {
                    this.setTextColor(TEXT_INVALID);
                }
            } catch (ResourceLocationException e) {
                this.setTextColor(TEXT_ERROR);
            }
            SoundWidget.this.detectChanges();
        }
    }

    private final class SoundSelectScreen extends SelectScreen<ResourceLocation> {
        private SoundSelectScreen() {
            super(SoundWidget.this.parent, new TextComponent("Select Sound Effect"));
        }

        @Override
        protected void onSave() {
            if (selected != null) {
                valueRef.set(selected.value);
                SoundWidget.this.syncWithValue();
            }
        }

        @Override
        protected boolean isSelected(ListWidget.Entry entry) {
            return entry.value.equals(valueRef.get());
        }

        @Override
        protected CompletableFuture<List<ResourceLocation>> getEntriesAsync() {
            ArrayList<ResourceLocation> list = new ArrayList<>(MC.getSoundManager().getAvailableSounds());
            Collections.sort(list);

            return CompletableFuture.completedFuture(list);
        }
    }
}
