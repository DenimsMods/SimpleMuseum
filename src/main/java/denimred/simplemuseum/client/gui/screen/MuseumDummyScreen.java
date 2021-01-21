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
import java.util.UUID;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.gui.widget.BetterImageButton;
import denimred.simplemuseum.client.gui.widget.ResourceFieldWidget;
import denimred.simplemuseum.common.entity.MuseumDummyEntity;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.messages.c2s.C2SConfigureDummy;
import denimred.simplemuseum.common.util.CheckedResource;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.resource.GeckoLibCache;

import static denimred.simplemuseum.client.gui.widget.ResourceFieldWidget.FOLDER_BUTTON_TEXTURE;

public class MuseumDummyScreen extends Screen {
    private static final int WIDTH = 300;
    private static final int MARGIN = 4;
    private static final String MODEL_PREFIX = "geo/";
    private static final String TEXTURE_PREFIX = "textures/";
    private static final String ANIMATIONS_PREFIX = "animations/";
    private static final int TEXT_VALID = 0xe0e0e0;
    private static final int TEXT_INVALID = 0xffff00;
    private static final int TEXT_ERROR = 0xff0000;
    public final int defaultRotation;
    private final Minecraft mc = Minecraft.getInstance();
    private final UUID uuid;
    private final CheckedResource<ResourceLocation> model;
    private final CheckedResource<ResourceLocation> texture;
    private final CheckedResource<ResourceLocation> animations;
    private final CheckedResource<String> selectedAnimation;
    private final SavedState state = new SavedState();
    private Button doneButton;
    private TextFieldWidget rotationField;
    private ResourceFieldWidget modelWidget;
    private ResourceFieldWidget textureWidget;
    private ResourceFieldWidget animationsWidget;
    private TextFieldWidget selectedAnimationField;

    public MuseumDummyScreen(MuseumDummyEntity dummy) {
        super(dummy.getDisplayName());
        uuid = dummy.getUniqueID();
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
            ITextComponent text,
            int offset) {
        drawString(
                matrixStack,
                font,
                text,
                field.x - font.getStringWidth(text.getUnformattedComponentText()) - MARGIN + offset,
                field.y + field.getHeightRealms() / 2 - font.FONT_HEIGHT / 2,
                0xA0A0A0);
    }

    @Override
    protected void init() {
        mc.keyboardListener.enableRepeatEvents(true);

        state.save();

        final int left = (width / 2) - (WIDTH / 2);
        final int modelFieldY = (height / 2) - 70 - MARGIN;
        modelWidget =
                new ResourceFieldWidget(
                        font,
                        left,
                        modelFieldY,
                        WIDTH,
                        20,
                        new StringTextComponent("Model Resource"),
                        MODEL_PREFIX,
                        model::validate,
                        button ->
                                mc.displayGuiScreen(
                                        new SelectResourceScreen(
                                                this,
                                                new StringTextComponent("Select Model Resource"),
                                                modelWidget)),
                        (button, matrixStack, mouseX, mouseY) ->
                                this.renderTooltip(
                                        matrixStack,
                                        new StringTextComponent("Select Model Resource"),
                                        mouseX,
                                        mouseY));

        final int textureFieldY =
                modelFieldY + modelWidget.getHeightRealms() + font.FONT_HEIGHT + MARGIN * 3;
        textureWidget =
                new ResourceFieldWidget(
                        font,
                        left,
                        textureFieldY,
                        WIDTH,
                        20,
                        new StringTextComponent("Texture Resource"),
                        TEXTURE_PREFIX,
                        texture::validate,
                        button ->
                                mc.displayGuiScreen(
                                        new SelectResourceScreen(
                                                this,
                                                new StringTextComponent("Select Texture Resource"),
                                                textureWidget)),
                        (button, matrixStack, mouseX, mouseY) ->
                                this.renderTooltip(
                                        matrixStack,
                                        new StringTextComponent("Select Texture Resource"),
                                        mouseX,
                                        mouseY));

        final int animationsFieldY =
                textureFieldY + textureWidget.getHeightRealms() + font.FONT_HEIGHT + MARGIN * 3;
        animationsWidget =
                new ResourceFieldWidget(
                        font,
                        left,
                        animationsFieldY,
                        WIDTH,
                        20,
                        new StringTextComponent("Animations Resource"),
                        ANIMATIONS_PREFIX,
                        animations::validate,
                        button ->
                                mc.displayGuiScreen(
                                        new SelectResourceScreen(
                                                this,
                                                new StringTextComponent(
                                                        "Select Animations Resource"),
                                                animationsWidget)),
                        (button, matrixStack, mouseX, mouseY) ->
                                this.renderTooltip(
                                        matrixStack,
                                        new StringTextComponent("Select Animations Resource"),
                                        mouseX,
                                        mouseY));

        final StringTextComponent selAnimFieldMsg = new StringTextComponent("Animation:");
        final StringTextComponent rotFieldMsg = new StringTextComponent("Rotation:");
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
        final BetterImageButton selectAnimButton =
                this.addButton(
                        new BetterImageButton(
                                selectedAnimationField.x + selectedAnimationField.getWidth() + 2,
                                selectedAnimationField.y,
                                20,
                                20,
                                0,
                                0,
                                20,
                                FOLDER_BUTTON_TEXTURE,
                                32,
                                64,
                                button ->
                                        mc.displayGuiScreen(
                                                new SelectAnimationScreen(
                                                        this,
                                                        new StringTextComponent("Select Animation"),
                                                        selectedAnimationField,
                                                        () -> this.getAnimLoc().orElse(null))),
                                (button, matrixStack, mouseX, mouseY) ->
                                        this.renderTooltip(
                                                matrixStack,
                                                new StringTextComponent("Select Animation"),
                                                mouseX,
                                                mouseY),
                                StringTextComponent.EMPTY));

        final int buttonsY = animationsFieldY + 40 + MARGIN * 5;
        doneButton =
                this.addButton(
                        new Button(
                                width / 2 - 4 - 150 + 2,
                                buttonsY,
                                150,
                                20,
                                DialogTexts.GUI_DONE,
                                b -> this.saveAndClose()));
        this.addButton(
                new Button(
                        width / 2 + 4 - 2,
                        buttonsY,
                        150,
                        20,
                        DialogTexts.GUI_CANCEL,
                        b -> this.closeScreen()));

        modelWidget.setChangeListener(this::checkDoneButton);
        children.add(modelWidget);

        textureWidget.setChangeListener(this::checkDoneButton);
        children.add(textureWidget);

        animationsWidget.setChangeListener(
                () -> {
                    this.checkDoneButton();
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
                    this.checkDoneButton();
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

    private void checkDoneButton() {
        doneButton.active =
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

        modelWidget.render(matrixStack, mouseX, mouseY, partialTicks);
        drawTitle(matrixStack, font, modelWidget);
        textureWidget.render(matrixStack, mouseX, mouseY, partialTicks);
        drawTitle(matrixStack, font, textureWidget);
        animationsWidget.render(matrixStack, mouseX, mouseY, partialTicks);
        drawTitle(matrixStack, font, animationsWidget);

        drawStringLeft(matrixStack, font, rotationField, rotationField.getMessage(), 0);
        rotationField.render(matrixStack, mouseX, mouseY, partialTicks);
        drawStringLeft(
                matrixStack, font, selectedAnimationField, selectedAnimationField.getMessage(), 0);
        selectedAnimationField.render(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
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
                            uuid,
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
            if (modelWidget != null) {
                modelState = modelWidget.getLocation();
            }
            if (textureWidget != null) {
                texState = textureWidget.getLocation();
            }
            if (animationsWidget != null) {
                animsState = animationsWidget.getLocation();
            }
            if (rotationField != null) {
                rotState = rotationField.getText();
            }
            if (selectedAnimationField != null) {
                selAnimState = selectedAnimationField.getText();
            }
        }

        public void load() {
            if (modelWidget != null) {
                modelWidget.setLocation(modelState, true);
            }
            if (textureWidget != null) {
                textureWidget.setLocation(texState, true);
            }
            if (animationsWidget != null) {
                animationsWidget.setLocation(animsState, true);
            }
            if (rotationField != null) {
                rotationField.setText(rotState);
            }
            if (selectedAnimationField != null) {
                selectedAnimationField.setText(selAnimState);
            }
        }
    }
}
