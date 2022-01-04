package denimred.simplemuseum.client.gui.widget;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.gui.screen.PuppetConfigScreen;
import denimred.simplemuseum.common.i18n.lang.GuiLang;

public class CopyPasteButtons extends NestedWidget {
    public static final ResourceLocation COPY_BUTTON_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/copy_button.png");
    public static final ResourceLocation PASTE_BUTTON_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/paste_button.png");

    public CopyPasteButtons(PuppetConfigScreen parent, int x, int y) {
        super(x, y, 40, 20, TextComponent.EMPTY);
        this.addChild(
                new IconButton(
                        x,
                        y,
                        20,
                        20,
                        COPY_BUTTON_TEXTURE,
                        0,
                        0,
                        64,
                        32,
                        20,
                        button -> parent.copy(),
                        parent::renderWidgetTooltip,
                        GuiLang.CLIPBOARD_COPY.asText()));
        this.addChild(
                new IconButton(
                        x + 20,
                        y,
                        20,
                        20,
                        PASTE_BUTTON_TEXTURE,
                        0,
                        0,
                        64,
                        32,
                        20,
                        button -> parent.paste(),
                        parent::renderWidgetTooltip,
                        GuiLang.CLIPBOARD_PASTE.asText()));
    }
}
