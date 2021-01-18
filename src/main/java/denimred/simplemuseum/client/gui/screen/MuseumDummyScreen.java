package denimred.simplemuseum.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.entity.MuseumDummyEntity;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.messages.c2s.C2SConfigureDummy;

public class MuseumDummyScreen extends Screen {
    private static final int WIDTH = 300;
    private static final int MARGIN = 4;
    private static final String MODEL_PREFIX = "geo/";
    private static final String TEXTURE_PREFIX = "textures/";
    private static final String ANIMATIONS_PREFIX = "animations/";
    private final Minecraft mc = Minecraft.getInstance();
    private final UUID uuid;
    private final MuseumDummyEntity.CheckedResource model;
    private final MuseumDummyEntity.CheckedResource texture;
    private final MuseumDummyEntity.CheckedResource animations;
    private final int defaultRotation;
    private Button doneButton;
    private Button cancelButton;
    private TextFieldWidget modelNamespace;
    private TextFieldWidget modelPath;
    private TextFieldWidget textureNamespace;
    private TextFieldWidget texturePath;
    private TextFieldWidget animationsNamespace;
    private TextFieldWidget animationsPath;
    private TextFieldWidget rotationField;

    public MuseumDummyScreen(MuseumDummyEntity dummy) {
        super(StringTextComponent.EMPTY);
        uuid = dummy.getUniqueID();
        model = dummy.getModel();
        texture = dummy.getTexture();
        animations = dummy.getAnimations();
        defaultRotation = Math.round(dummy.rotationYaw);
    }

    private static void drawStringAbove(
            MatrixStack matrixStack,
            FontRenderer font,
            TextFieldWidget field,
            ITextComponent text) {
        drawCenteredString(
                matrixStack,
                font,
                text,
                field.x + field.getAdjustedWidth() / 2,
                field.y - font.FONT_HEIGHT - MARGIN,
                field.getFGColor());
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
                field.getFGColor());
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
    }

    @Override
    protected void init() {
        mc.keyboardListener.enableRepeatEvents(true);

        final int modelPrefixWidth = font.getStringWidth(MODEL_PREFIX);
        final int modelFieldY = (height / 2) - 10 - 20 - MARGIN;
        modelNamespace =
                new TextFieldWidget(
                        font,
                        (width / 2) - (WIDTH / 2),
                        modelFieldY,
                        (WIDTH / 4) - (MARGIN / 2),
                        20,
                        StringTextComponent.EMPTY);
        modelPath =
                new TextFieldWidget(
                        font,
                        (width / 2) - (WIDTH / 2) + (WIDTH / 4) + MARGIN / 2 + modelPrefixWidth,
                        modelFieldY,
                        ((WIDTH / 4) * 3) - (MARGIN / 2) - modelPrefixWidth,
                        20,
                        new StringTextComponent("Model:"));

        final int texturePrefixWidth = font.getStringWidth(TEXTURE_PREFIX);
        final int textureFieldY = (height / 2) - 10;
        textureNamespace =
                new TextFieldWidget(
                        font,
                        (width / 2) - (WIDTH / 2),
                        textureFieldY,
                        (WIDTH / 4) - (MARGIN / 2),
                        20,
                        StringTextComponent.EMPTY);
        texturePath =
                new TextFieldWidget(
                        font,
                        (width / 2) - (WIDTH / 2) + (WIDTH / 4) + MARGIN / 2 + texturePrefixWidth,
                        textureFieldY,
                        ((WIDTH / 4) * 3) - (MARGIN / 2) - texturePrefixWidth,
                        20,
                        new StringTextComponent("Texture:"));

        final int animationPrefixWidth = font.getStringWidth(ANIMATIONS_PREFIX);
        final int animationsFieldY = (height / 2) - 10 + 20 + MARGIN;
        animationsNamespace =
                new TextFieldWidget(
                        font,
                        (width / 2) - (WIDTH / 2),
                        animationsFieldY,
                        (WIDTH / 4) - (MARGIN / 2),
                        20,
                        StringTextComponent.EMPTY);
        animationsPath =
                new TextFieldWidget(
                        font,
                        (width / 2) - (WIDTH / 2) + (WIDTH / 4) + MARGIN / 2 + animationPrefixWidth,
                        animationsFieldY,
                        ((WIDTH / 4) * 3) - (MARGIN / 2) - animationPrefixWidth,
                        20,
                        new StringTextComponent("Animations:"));

        rotationField =
                new TextFieldWidget(
                        font,
                        width / 2 - 20,
                        animationsFieldY + 20 + MARGIN * 2,
                        40,
                        20,
                        new StringTextComponent("Rotation:"));

        final int buttonsY = animationsFieldY + 40 + MARGIN * 4;
        doneButton =
                this.addButton(
                        new Button(
                                width / 2 - 4 - 150 + 2,
                                buttonsY,
                                150,
                                20,
                                DialogTexts.GUI_DONE,
                                b -> this.saveAndClose()));
        cancelButton =
                this.addButton(
                        new Button(
                                width / 2 + 4 - 2,
                                buttonsY,
                                150,
                                20,
                                DialogTexts.GUI_CANCEL,
                                b -> this.closeScreen()));

        final Consumer<String> modelResponder =
                s -> locFieldResponder(model, modelNamespace, modelPath, this::getModelLoc);
        final ResourceLocation modelLocation = model.getDirect();
        modelNamespace.setMaxStringLength(16250);
        modelNamespace.setResponder(modelResponder);
        modelNamespace.setText(modelLocation.getNamespace());
        children.add(modelNamespace);
        modelPath.setMaxStringLength(16250);
        modelPath.setResponder(modelResponder);
        modelPath.setText(modelLocation.getPath().replaceFirst(Pattern.quote(MODEL_PREFIX), ""));
        children.add(modelPath);

        final Consumer<String> textureResponder =
                s -> locFieldResponder(texture, textureNamespace, texturePath, this::getTexLoc);
        final ResourceLocation textureLocation = texture.getDirect();
        textureNamespace.setMaxStringLength(16250);
        textureNamespace.setResponder(textureResponder);
        textureNamespace.setText(textureLocation.getNamespace());
        children.add(textureNamespace);
        texturePath.setMaxStringLength(16250);
        texturePath.setResponder(textureResponder);
        texturePath.setText(
                textureLocation.getPath().replaceFirst(Pattern.quote(TEXTURE_PREFIX), ""));
        children.add(texturePath);

        final Consumer<String> animationsResponder =
                s ->
                        locFieldResponder(
                                animations, animationsNamespace, animationsPath, this::getAnimLoc);
        final ResourceLocation animationsLocation = animations.getDirect();
        animationsNamespace.setMaxStringLength(16250);
        animationsNamespace.setResponder(animationsResponder);
        animationsNamespace.setText(animationsLocation.getNamespace());
        animationsNamespace.setEnabled(false);
        children.add(animationsNamespace);
        animationsPath.setMaxStringLength(16250);
        animationsPath.setResponder(animationsResponder);
        animationsPath.setText(
                animationsLocation.getPath().replaceFirst(Pattern.quote(ANIMATIONS_PREFIX), ""));
        animationsPath.setEnabled(false);
        children.add(animationsPath);

        rotationField.setMaxStringLength(128);
        rotationField.setResponder(
                s -> {
                    if (this.getRotation().isPresent()) {
                        rotationField.setTextColor(0xe0e0e0);
                    } else {
                        rotationField.setTextColor(0xff0000);
                    }
                    this.checkDoneButton();
                });
        rotationField.setText(String.valueOf(defaultRotation));
        children.add(rotationField);
    }

    private void locFieldResponder(
            MuseumDummyEntity.CheckedResource resource,
            TextFieldWidget namespace,
            TextFieldWidget path,
            Supplier<Optional<ResourceLocation>> locationSupplier) {
        final Optional<ResourceLocation> location = locationSupplier.get();
        if (location.isPresent()) {
            if (resource.check(location.get())) {
                namespace.setTextColor(0xe0e0e0);
                path.setTextColor(0xe0e0e0);
            } else {
                namespace.setTextColor(0xffff00);
                path.setTextColor(0xffff00);
            }
        } else {
            namespace.setTextColor(0xff0000);
            path.setTextColor(0xff0000);
        }
        this.checkDoneButton();
    }

    private void checkDoneButton() {
        doneButton.active =
                this.getModelLoc().isPresent()
                        && this.getTexLoc().isPresent()
                        && this.getAnimLoc().isPresent()
                        && this.getRotation().isPresent();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        final String modelNamespaceText = modelNamespace.getText();
        final String modelPathText = modelPath.getText();
        final String textureNamespaceText = textureNamespace.getText();
        final String texturePathText = texturePath.getText();
        final String animationsNamespaceText = animationsNamespace.getText();
        final String animationsPathText = animationsPath.getText();
        final String rotationFieldText = rotationField.getText();
        super.resize(minecraft, width, height);
        modelNamespace.setText(modelNamespaceText);
        modelPath.setText(modelPathText);
        textureNamespace.setText(textureNamespaceText);
        texturePath.setText(texturePathText);
        animationsNamespace.setText(animationsNamespaceText);
        animationsPath.setText(animationsPathText);
        rotationField.setText(rotationFieldText);
    }

    @Override
    public void tick() {
        modelNamespace.tick();
        modelPath.tick();
        textureNamespace.tick();
        texturePath.tick();
        animationsNamespace.tick();
        animationsPath.tick();
        rotationField.tick();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        drawStringAbove(matrixStack, font, modelNamespace, new StringTextComponent("Namespace"));
        drawStringAbove(matrixStack, font, modelPath, new StringTextComponent("File Path"));
        drawStringLeft(matrixStack, font, modelNamespace, modelPath.getMessage(), 0);
        modelNamespace.render(matrixStack, mouseX, mouseY, partialTicks);
        drawStringLeft(matrixStack, font, modelPath, new StringTextComponent(MODEL_PREFIX), MARGIN);
        modelPath.render(matrixStack, mouseX, mouseY, partialTicks);
        drawStringLeft(matrixStack, font, textureNamespace, texturePath.getMessage(), 0);
        textureNamespace.render(matrixStack, mouseX, mouseY, partialTicks);
        drawStringLeft(
                matrixStack, font, texturePath, new StringTextComponent(TEXTURE_PREFIX), MARGIN);
        texturePath.render(matrixStack, mouseX, mouseY, partialTicks);
        drawStringLeft(matrixStack, font, animationsNamespace, animationsPath.getMessage(), 0);
        animationsNamespace.render(matrixStack, mouseX, mouseY, partialTicks);
        drawStringLeft(
                matrixStack,
                font,
                animationsPath,
                new StringTextComponent(ANIMATIONS_PREFIX),
                MARGIN);
        animationsPath.render(matrixStack, mouseX, mouseY, partialTicks);
        drawStringLeft(matrixStack, font, rotationField, rotationField.getMessage(), 0);
        rotationField.render(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        mc.keyboardListener.enableRepeatEvents(false);
    }

    protected void saveAndClose() {
        try {
            MuseumNetworking.CHANNEL.sendToServer(
                    new C2SConfigureDummy(
                            uuid,
                            this.getRotation().orElse(defaultRotation),
                            this.getModelLoc().orElse(model.getDirect()),
                            this.getTexLoc().orElse(texture.getDirect()),
                            this.getAnimLoc().orElse(animations.getDirect())));
        } catch (ResourceLocationException e) {
            SimpleMuseum.LOGGER.error("Failed to send dummy configuration to server", e);
        }
        this.closeScreen();
    }

    protected Optional<ResourceLocation> getModelLoc() {
        try {
            final ResourceLocation fallback = model.getFallback();
            final String namespace = modelNamespace.getText();
            final String path = modelPath.getText();
            return Optional.of(
                    new ResourceLocation(
                            namespace.isEmpty() ? fallback.getNamespace() : namespace,
                            MODEL_PREFIX
                                    + (path.isEmpty()
                                            ? fallback.getPath()
                                                    .replaceFirst(Pattern.quote(MODEL_PREFIX), "")
                                            : path)));
        } catch (ResourceLocationException e) {
            return Optional.empty();
        }
    }

    protected Optional<ResourceLocation> getTexLoc() {
        try {
            final ResourceLocation fallback = texture.getFallback();
            final String namespace = textureNamespace.getText();
            final String path = texturePath.getText();
            return Optional.of(
                    new ResourceLocation(
                            namespace.isEmpty() ? fallback.getNamespace() : namespace,
                            TEXTURE_PREFIX
                                    + (path.isEmpty()
                                            ? fallback.getPath()
                                                    .replaceFirst(Pattern.quote(TEXTURE_PREFIX), "")
                                            : path)));
        } catch (ResourceLocationException e) {
            return Optional.empty();
        }
    }

    protected Optional<ResourceLocation> getAnimLoc() {
        try {
            final ResourceLocation fallback = animations.getFallback();
            final String namespace = animationsNamespace.getText();
            final String path = animationsPath.getText();
            return Optional.of(
                    new ResourceLocation(
                            namespace.isEmpty() ? fallback.getNamespace() : namespace,
                            ANIMATIONS_PREFIX
                                    + (path.isEmpty()
                                            ? fallback.getPath()
                                                    .replaceFirst(
                                                            Pattern.quote(ANIMATIONS_PREFIX), "")
                                            : path)));
        } catch (ResourceLocationException e) {
            return Optional.empty();
        }
    }

    protected Optional<Integer> getRotation() {
        try {
            final String text = rotationField.getText();
            return Optional.of(Integer.parseInt(text.isEmpty() ? "0" : text));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
