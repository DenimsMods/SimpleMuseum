package denimred.simplemuseum.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;

public class ResourceFieldWidget extends Widget {
    public static final int MARGIN = 4;
    public static final int MAX_LENGTH = 0x7FFF;
    public static final ResourceLocation FOLDER_BUTTON_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/folder_button.png");
    protected static final Minecraft MC = Minecraft.getInstance();
    public final int TEXT_ERROR = 0xFF0000;
    public final int TEST_VALID = 0xE0E0E0;
    public final int TEST_INVALID = 0xFFFF00;
    protected final FontRenderer font;
    protected final String pathPrefix;
    protected final TextFieldWidget namespaceField;
    protected final TextFieldWidget pathField;
    protected final BetterImageButton openListButton;
    protected final Pattern pathPrefixPattern;
    private final Predicate<ResourceLocation> validator;
    protected int color;
    @Nullable private Runnable changeListener;

    public ResourceFieldWidget(
            FontRenderer font,
            int x,
            int y,
            int width,
            int height,
            ITextComponent title,
            String pathPrefix,
            Predicate<ResourceLocation> validator,
            Button.IPressable buttonAction,
            Button.ITooltip buttonTooltip) {
        super(x, y, width, height, title);
        this.font = font;
        this.pathPrefix = pathPrefix;
        this.pathPrefixPattern = Pattern.compile("^" + Pattern.quote(pathPrefix));
        this.validator = validator;

        final int namespaceWidth = (width / 4) - (MARGIN / 2);
        final int pathPrefixWidth = (int) (font.getStringWidth(pathPrefix) + (MARGIN * 1.25));
        final int pathWidth = width - namespaceWidth - pathPrefixWidth - 21;
        namespaceField =
                new TextFieldWidget(font, x, y, namespaceWidth, height, StringTextComponent.EMPTY);
        pathField =
                new TextFieldWidget(
                        font,
                        x + namespaceWidth + pathPrefixWidth,
                        y,
                        pathWidth,
                        height,
                        StringTextComponent.EMPTY);
        openListButton =
                new BetterImageButton(
                        pathField.x + pathWidth + 2,
                        y,
                        20,
                        20,
                        0,
                        0,
                        20,
                        FOLDER_BUTTON_TEXTURE,
                        32,
                        64,
                        buttonAction,
                        buttonTooltip,
                        StringTextComponent.EMPTY);

        namespaceField.setResponder(n -> this.respondFields(n, pathField.getText()));
        namespaceField.setMaxStringLength(MAX_LENGTH / 2);

        pathField.setResponder(p -> this.respondFields(namespaceField.getText(), p));
        pathField.setMaxStringLength((MAX_LENGTH / 2) - pathPrefix.length());
    }

    // ResourceLocation.tryCreate() is annoying and slightly inefficient,
    // so clearly I had to reimplement it
    @Nullable
    private static ResourceLocation tryCreateLoc(String namespace, String path) {
        try {
            return new ResourceLocation(namespace, path);
        } catch (ResourceLocationException e) {
            return null;
        }
    }

    public boolean validate(ResourceLocation loc) {
        return validator.test(loc);
    }

    public void setChangeListener(@Nullable Runnable changeListener) {
        this.changeListener = changeListener;
    }

    public void setLocation(@Nullable ResourceLocation loc, boolean trimPathPrefix) {
        if (loc == null) {
            namespaceField.setText("");
            pathField.setText("");
        } else {
            namespaceField.setText(loc.getNamespace());
            if (trimPathPrefix) {
                pathField.setText(pathPrefixPattern.matcher(loc.getPath()).replaceFirst(""));
            } else {
                pathField.setText(loc.getPath());
            }
        }
        namespaceField.setCursorPositionZero();
        pathField.setCursorPositionZero();
    }

    private void respondFields(String namespace, String path) {
        final ResourceLocation loc = tryCreateLoc(namespace, pathPrefix.concat(path));
        if (loc == null) {
            color = TEXT_ERROR;
        } else {
            if (MC.getResourceManager().hasResource(loc)) {
                color = TEST_VALID;
            } else {
                color = TEST_INVALID;
            }
        }
        namespaceField.setTextColor(color);
        pathField.setTextColor(color);
        if (changeListener != null) {
            changeListener.run();
        }
    }

    @Nullable
    public ResourceLocation getLocation() {
        return tryCreateLoc(namespaceField.getText(), pathPrefix.concat(pathField.getText()));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return namespaceField.keyPressed(keyCode, scanCode, modifiers)
                || openListButton.keyPressed(keyCode, scanCode, modifiers)
                || pathField.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return namespaceField.charTyped(codePoint, modifiers)
                || pathField.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return namespaceField.mouseClicked(mouseX, mouseY, button)
                || openListButton.mouseClicked(mouseX, mouseY, button)
                || pathField.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        namespaceField.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        pathField.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        font.drawStringWithShadow(
                matrixStack,
                pathPrefix,
                x + namespaceField.getWidth() + MARGIN,
                y + (height / 2.0F) - (font.FONT_HEIGHT / 2.0F),
                color);
        openListButton.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean changeFocus(boolean focus) {
        return namespaceField.changeFocus(focus)
                || openListButton.changeFocus(focus)
                || pathField.changeFocus(focus);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return namespaceField.isMouseOver(mouseX, mouseY)
                || openListButton.isMouseOver(mouseX, mouseY)
                || pathField.isMouseOver(mouseX, mouseY);
    }

    public void tick() {
        namespaceField.tick();
        pathField.tick();
    }

    public String getPathPrefix() {
        return pathPrefix;
    }
}
