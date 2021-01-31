package denimred.simplemuseum.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Optional;
import java.util.StringJoiner;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.gui.widget.IconButton;
import denimred.simplemuseum.client.gui.widget.MovementButtons;
import denimred.simplemuseum.client.gui.widget.ResourceFieldWidget;
import denimred.simplemuseum.common.entity.MuseumDummyEntity;
import denimred.simplemuseum.common.init.MuseumLang;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.messages.c2s.C2SConfigureDummy;
import denimred.simplemuseum.common.util.CheckedResource;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class ConfigureDummyScreen extends Screen {
    public static final ResourceLocation COPY_BUTTON_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/copy_button.png");
    public static final ResourceLocation PASTE_BUTTON_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/paste_button.png");
    private static final int WIDTH = 300;
    private static final int MARGIN = 4;
    private static final String MODEL_PREFIX = "geo/";
    private static final String TEXTURE_PREFIX = "textures/";
    private static final String ANIMATIONS_PREFIX = "animations/";
    private static final int TEXT_VALID = 0xe0e0e0;
    private static final int TEXT_INVALID = 0xffff00;
    private static final int TEXT_ERROR = 0xff0000;
    public final int defaultRotation;
    @Nullable private final Screen parent;
    private final Minecraft mc = Minecraft.getInstance(); // Parent's is nullable for no good reason
    private final MuseumDummyEntity dummy;
    private final CheckedResource<ResourceLocation> model;
    private final CheckedResource<ResourceLocation> texture;
    private final CheckedResource<ResourceLocation> animations;
    private final CheckedResource<String> selectedAnimation;
    private final SavedState state = new SavedState();
    private IconButton copyButton;
    private Button doneButton;
    private ResourceFieldWidget modelWidget;
    private ResourceFieldWidget textureWidget;
    private ResourceFieldWidget animationsWidget;
    private TextFieldWidget selectedAnimationField;
    private TextFieldWidget rotationField;
    private MovementButtons mb;

    public ConfigureDummyScreen(MuseumDummyEntity dummy, @Nullable Screen parent) {
        super(dummy.getDisplayName());
        this.dummy = dummy;
        this.parent = parent;
        model = dummy.getModelLocation();
        texture = dummy.getTextureLocation();
        animations = dummy.getAnimationsLocation();
        selectedAnimation = dummy.getSelectedAnimation();
        defaultRotation = Math.round(dummy.rotationYaw);
        state.init();
    }

    private static void drawTitle(MatrixStack matrixStack, FontRenderer font, Widget widget) {
        drawString(
                matrixStack,
                font,
                widget.getMessage(),
                widget.x,
                widget.y - font.FONT_HEIGHT - MARGIN / 2,
                0xA0A0A0);
    }

    private static void drawStringLeft(
            MatrixStack matrixStack,
            FontRenderer font,
            TextFieldWidget field,
            ITextComponent text) {
        drawString(
                matrixStack,
                font,
                text,
                field.x - font.getStringPropertyWidth(text) - MARGIN,
                field.y + field.getHeightRealms() / 2 - font.FONT_HEIGHT / 2,
                0xA0A0A0);
    }

    private void copy() {
        final StringJoiner joiner = new StringJoiner("|");
        joiner.add(this.getModelLoc().map(String::valueOf).orElse(""));
        joiner.add(this.getTexLoc().map(String::valueOf).orElse(""));
        joiner.add(this.getAnimLoc().map(String::valueOf).orElse(""));
        joiner.add(selectedAnimationField.getText());
        joiner.add(this.getRotation().map(String::valueOf).orElse(""));
        mc.keyboardListener.setClipboardString(joiner.toString());
    }

    private void paste() {
        final String clip = mc.keyboardListener.getClipboardString();
        final String[] split = clip.split("\\|", -1);
        final int length = split.length;
        if (length > 0) modelWidget.setLocation(split[0], true, true);
        if (length > 1) textureWidget.setLocation(split[1], true, true);
        if (length > 2) animationsWidget.setLocation(split[2], true, true);
        if (length > 3) selectedAnimationField.setText(split[3]);
        if (length > 4) rotationField.setText(split[4]);
    }

    @Override
    protected void init() {
        mc.keyboardListener.enableRepeatEvents(true);

        state.save();

        final int center = (width / 2);
        final int left = center - (WIDTH / 2);
        final int top = (height / 2) - 100;

        // TODO: This doesn't do anything lol
        mb =
                new MovementButtons(
                        left - 70,
                        top,
                        new StringTextComponent("todo"),
                        MovementButtons::getName,
                        i -> {},
                        this::renderWidgetTooltip);
        children.add(mb);

        copyButton =
                this.addButton(
                        new IconButton(
                                center - 20 - MARGIN,
                                top,
                                20,
                                20,
                                0,
                                0,
                                20,
                                COPY_BUTTON_TEXTURE,
                                32,
                                64,
                                button -> this.copy(),
                                this::renderWidgetTooltip,
                                MuseumLang.GUI_CLIPBOARD_COPY.asText()));
        this.addButton(
                new IconButton(
                        center + MARGIN,
                        top,
                        20,
                        20,
                        0,
                        0,
                        20,
                        PASTE_BUTTON_TEXTURE,
                        32,
                        64,
                        button -> this.paste(),
                        this::renderWidgetTooltip,
                        MuseumLang.GUI_CLIPBOARD_PASTE.asText()));

        final int modelFieldY = top + 30 + MARGIN;
        modelWidget =
                new ResourceFieldWidget(
                        font,
                        left,
                        modelFieldY,
                        WIDTH,
                        20,
                        MuseumLang.GUI_DUMMY_MODEL.asText(),
                        MuseumLang.GUI_DUMMY_MODEL_SELECT.asText(),
                        MODEL_PREFIX,
                        model::validate,
                        button ->
                                mc.displayGuiScreen(
                                        new SelectResourceScreen(this, button, modelWidget)),
                        this::renderWidgetTooltip);

        final int textureFieldY =
                modelFieldY + modelWidget.getHeightRealms() + font.FONT_HEIGHT + MARGIN * 3;
        textureWidget =
                new ResourceFieldWidget(
                        font,
                        left,
                        textureFieldY,
                        WIDTH,
                        20,
                        MuseumLang.GUI_DUMMY_TEXTURE.asText(),
                        MuseumLang.GUI_DUMMY_TEXTURE_SELECT.asText(),
                        TEXTURE_PREFIX,
                        texture::validate,
                        button ->
                                mc.displayGuiScreen(
                                        new SelectResourceScreen(this, button, textureWidget)),
                        this::renderWidgetTooltip);

        final int animationsFieldY =
                textureFieldY + textureWidget.getHeightRealms() + font.FONT_HEIGHT + MARGIN * 3;
        animationsWidget =
                new ResourceFieldWidget(
                        font,
                        left,
                        animationsFieldY,
                        WIDTH,
                        20,
                        MuseumLang.GUI_DUMMY_ANIMATIONS.asText(),
                        MuseumLang.GUI_DUMMY_ANIMATIONS_SELECT.asText(),
                        ANIMATIONS_PREFIX,
                        animations::validate,
                        button ->
                                mc.displayGuiScreen(
                                        new SelectResourceScreen(this, button, animationsWidget)),
                        this::renderWidgetTooltip);

        final ITextComponent selAnimFieldMsg = MuseumLang.GUI_DUMMY_SELECTED_ANIMATION.asText();
        final ITextComponent rotFieldMsg = MuseumLang.GUI_DUMMY_ROTATION.asText();
        final int selAnimFieldWidth = font.getStringPropertyWidth(selAnimFieldMsg);
        final int rotFieldWidth = font.getStringPropertyWidth(rotFieldMsg);
        final int miscY = animationsFieldY + 20 + MARGIN * 3;
        rotationField =
                new TextFieldWidget(
                        font,
                        left + rotFieldWidth + 3,
                        miscY,
                        (WIDTH / 8) - (MARGIN / 2),
                        20,
                        rotFieldMsg);
        selectedAnimationField =
                new TextFieldWidget(
                        font,
                        left + rotFieldWidth + (WIDTH / 8) + (MARGIN * 6) + selAnimFieldWidth,
                        miscY,
                        WIDTH
                                - (rotFieldWidth + (WIDTH / 8) + (MARGIN * 6) + selAnimFieldWidth)
                                - 21,
                        20,
                        selAnimFieldMsg);
        final IconButton selectAnimButton =
                this.addButton(
                        new IconButton(
                                selectedAnimationField.x + selectedAnimationField.getWidth() + 2,
                                selectedAnimationField.y,
                                20,
                                20,
                                0,
                                0,
                                20,
                                ResourceFieldWidget.FOLDER_BUTTON_TEXTURE,
                                32,
                                64,
                                button ->
                                        mc.displayGuiScreen(
                                                new SelectAnimationScreen(
                                                        this,
                                                        button,
                                                        selectedAnimationField,
                                                        () -> this.getAnimLoc().orElse(null))),
                                this::renderWidgetTooltip,
                                MuseumLang.GUI_DUMMY_SELECTED_ANIMATION_SELECT.asText()));

        final int exitButtonsY = animationsFieldY + 40 + MARGIN * 5;
        doneButton =
                this.addButton(
                        new Button(
                                width / 2 - 4 - 150 + 2,
                                exitButtonsY,
                                150,
                                20,
                                DialogTexts.GUI_DONE,
                                b -> this.saveAndClose()));
        this.addButton(
                new Button(
                        width / 2 + 4 - 2,
                        exitButtonsY,
                        150,
                        20,
                        DialogTexts.GUI_CANCEL,
                        b -> this.closeScreen()));

        modelWidget.setChangeListener(this::checkAcceptableState);
        children.add(modelWidget);

        textureWidget.setChangeListener(this::checkAcceptableState);
        children.add(textureWidget);

        animationsWidget.setChangeListener(
                () -> {
                    this.checkAcceptableState();
                    // Triggers the responder
                    selectedAnimationField.setText(selectedAnimationField.getText());
                });
        children.add(animationsWidget);

        rotationField.setMaxStringLength(128);
        rotationField.setResponder(
                s -> {
                    if (this.getRotation().isPresent()) {
                        rotationField.setTextColor(TEXT_VALID);
                    } else {
                        rotationField.setTextColor(TEXT_ERROR);
                    }
                    this.checkAcceptableState();
                });
        children.add(rotationField);

        selectedAnimationField.setMaxStringLength(32500);
        selectedAnimationField.setResponder(
                s -> {
                    // This is needed since selectedAnimation isn't set
                    // until the done button is pressed
                    final Optional<ResourceLocation> animLoc = this.getAnimLoc();
                    if (animLoc.isPresent()) {
                        final AnimationFile animFile =
                                GeckoLibCache.getInstance().getAnimations().get(animLoc.get());
                        final boolean fileExists = animFile != null;
                        if (fileExists && (s.isEmpty() || animFile.getAnimation(s) != null)) {
                            selectedAnimationField.setTextColor(TEXT_VALID);
                        } else {
                            selectedAnimationField.setTextColor(TEXT_INVALID);
                        }
                        selectAnimButton.active = fileExists;
                    } else {
                        selectAnimButton.active = false;
                        selectedAnimationField.setTextColor(TEXT_ERROR);
                    }
                });
        children.add(selectedAnimationField);

        state.load();
    }

    private void renderWidgetTooltip(
            Widget widget, MatrixStack matrixStack, int mouseX, int mouseY) {
        this.renderTooltip(matrixStack, widget.getMessage(), mouseX, mouseY);
    }

    private void checkAcceptableState() {
        doneButton.active =
                copyButton.active =
                        this.getModelLoc().isPresent()
                                && this.getTexLoc().isPresent()
                                && this.getAnimLoc().isPresent()
                                && this.getRotation().isPresent();
    }

    @Override
    public void tick() {
        modelWidget.tick();
        textureWidget.tick();
        animationsWidget.tick();
        rotationField.tick();
        selectedAnimationField.tick();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        drawCenteredString(
                matrixStack,
                font,
                title.copyRaw().mergeStyle(TextFormatting.UNDERLINE),
                width / 2,
                10,
                0xFFFFFF);

        mb.render(matrixStack, mouseX, mouseY, partialTicks);

        modelWidget.render(matrixStack, mouseX, mouseY, partialTicks);
        drawTitle(matrixStack, font, modelWidget);
        textureWidget.render(matrixStack, mouseX, mouseY, partialTicks);
        drawTitle(matrixStack, font, textureWidget);
        animationsWidget.render(matrixStack, mouseX, mouseY, partialTicks);
        drawTitle(matrixStack, font, animationsWidget);

        drawStringLeft(matrixStack, font, rotationField, rotationField.getMessage());
        rotationField.render(matrixStack, mouseX, mouseY, partialTicks);
        drawStringLeft(
                matrixStack, font, selectedAnimationField, selectedAnimationField.getMessage());
        selectedAnimationField.render(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void closeScreen() {
        mc.displayGuiScreen(parent);
    }

    @Override
    public void onClose() {
        mc.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected void saveAndClose() {
        try {
            MuseumNetworking.CHANNEL.sendToServer(
                    new C2SConfigureDummy(
                            dummy.getUniqueID(),
                            this.getRotation().orElse(defaultRotation),
                            this.getModelLoc().orElse(model.getDirect()),
                            this.getTexLoc().orElse(texture.getDirect()),
                            this.getAnimLoc().orElse(animations.getDirect()),
                            selectedAnimationField.getText()));
        } catch (ResourceLocationException e) {
            SimpleMuseum.LOGGER.error("Failed to send dummy configuration to server", e);
        }
        this.closeScreen();
    }

    protected Optional<Integer> getRotation() {
        try {
            final String text = rotationField.getText();
            return Optional.of(Integer.parseInt(text.isEmpty() ? "0" : text));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    protected Optional<ResourceLocation> getModelLoc() {
        return Optional.ofNullable(modelWidget.getLocation());
    }

    protected Optional<ResourceLocation> getTexLoc() {
        return Optional.ofNullable(textureWidget.getLocation());
    }

    protected Optional<ResourceLocation> getAnimLoc() {
        return Optional.ofNullable(animationsWidget.getLocation());
    }

    private class SavedState {
        private ResourceLocation modelState;
        private ResourceLocation texState;
        private ResourceLocation animsState;
        private String rotState;
        private String selAnimState;

        public void init() {
            modelState = model.getDirect();
            texState = texture.getDirect();
            animsState = animations.getDirect();
            rotState = String.valueOf(defaultRotation);
            selAnimState = selectedAnimation.getDirect();
        }

        public void save() {
            if (modelWidget != null) modelState = modelWidget.getLocation();
            if (textureWidget != null) texState = textureWidget.getLocation();
            if (animationsWidget != null) animsState = animationsWidget.getLocation();
            if (rotationField != null) rotState = rotationField.getText();
            if (selectedAnimationField != null) selAnimState = selectedAnimationField.getText();
        }

        public void load() {
            if (modelWidget != null) modelWidget.setLocation(modelState, true);
            if (textureWidget != null) textureWidget.setLocation(texState, true);
            if (animationsWidget != null) animationsWidget.setLocation(animsState, true);
            if (rotationField != null) rotationField.setText(rotState);
            if (selectedAnimationField != null) selectedAnimationField.setText(selAnimState);
        }
    }
}
