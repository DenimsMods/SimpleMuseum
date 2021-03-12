package denimred.simplemuseum.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.ITextComponent;

import java.util.Optional;
import java.util.StringJoiner;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.gui.widget.IconButton;
import denimred.simplemuseum.client.gui.widget.ResourceFieldWidget;
import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.entity.MuseumPuppetEntity;
import denimred.simplemuseum.common.init.MuseumLang;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.messages.c2s.C2SConfigurePuppet;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class ConfigurePuppetScreen extends MuseumPuppetScreen {
    public static final ResourceLocation COPY_BUTTON_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/copy_button.png");
    public static final ResourceLocation PASTE_BUTTON_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/paste_button.png");
    private static final int WIDTH = 300;
    private static final String MODEL_PREFIX = "geo/";
    private static final String TEXTURE_PREFIX = "textures/";
    private static final String ANIMATIONS_PREFIX = "animations/";
    private final SavedState state;
    private IconButton copyButton;
    private Button doneButton;
    private ResourceFieldWidget modelWidget;
    private ResourceFieldWidget textureWidget;
    private ResourceFieldWidget animationsWidget;
    private TextFieldWidget idleAnimationField;

    public ConfigurePuppetScreen(MuseumPuppetEntity puppet, @Nullable Screen parent) {
        super(puppet, parent);
        state = new SavedState();
        ClientUtil.setLastPuppetScreen(ConfigurePuppetScreen::new);
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

    private void copy() {
        final StringJoiner joiner = new StringJoiner("|");
        joiner.add(this.getModelLoc().map(String::valueOf).orElse(""));
        joiner.add(this.getTexLoc().map(String::valueOf).orElse(""));
        joiner.add(this.getAnimLoc().map(String::valueOf).orElse(""));
        joiner.add(idleAnimationField.getText());
        mc.keyboardListener.setClipboardString(joiner.toString());
    }

    private void paste() {
        final String clip = mc.keyboardListener.getClipboardString();
        final String[] split = clip.split("\\|", -1);
        final int length = split.length;
        if (length > 0) modelWidget.setLocation(split[0], true, true);
        if (length > 1) textureWidget.setLocation(split[1], true, true);
        if (length > 2) animationsWidget.setLocation(split[2], true, true);
        if (length > 3) idleAnimationField.setText(split[3]);
    }

    @Override
    protected void init() {
        state.save();

        final int center = (width / 2);
        final int left = center - (WIDTH / 2);
        final int top = (height / 2) - 100;

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
                this.addListener(
                        new ResourceFieldWidget(
                                font,
                                left,
                                modelFieldY,
                                WIDTH,
                                20,
                                MuseumLang.GUI_PUPPET_CONFIG_MODEL.asText(),
                                MuseumLang.GUI_PUPPET_CONFIG_MODEL_SELECT.asText(),
                                MODEL_PREFIX,
                                puppet.sourceManager.model::validate,
                                button ->
                                        mc.displayGuiScreen(
                                                new SelectResourceScreen(
                                                        this, button, modelWidget)),
                                this::renderWidgetTooltip));

        final int textureFieldY =
                modelFieldY + modelWidget.getHeight() + font.FONT_HEIGHT + MARGIN * 3;
        textureWidget =
                this.addListener(
                        new ResourceFieldWidget(
                                font,
                                left,
                                textureFieldY,
                                WIDTH,
                                20,
                                MuseumLang.GUI_PUPPET_CONFIG_TEX.asText(),
                                MuseumLang.GUI_PUPPET_CONFIG_TEX_SELECT.asText(),
                                TEXTURE_PREFIX,
                                puppet.sourceManager.texture::validate,
                                button ->
                                        mc.displayGuiScreen(
                                                new SelectResourceScreen(
                                                        this, button, textureWidget)),
                                this::renderWidgetTooltip));

        final int animationsFieldY =
                textureFieldY + textureWidget.getHeight() + font.FONT_HEIGHT + MARGIN * 3;
        animationsWidget =
                this.addListener(
                        new ResourceFieldWidget(
                                font,
                                left,
                                animationsFieldY,
                                WIDTH,
                                20,
                                MuseumLang.GUI_PUPPET_CONFIG_ANIMS.asText(),
                                MuseumLang.GUI_PUPPET_CONFIG_ANIMS_SELECT.asText(),
                                ANIMATIONS_PREFIX,
                                puppet.sourceManager.animations::validate,
                                button ->
                                        mc.displayGuiScreen(
                                                new SelectResourceScreen(
                                                        this, button, animationsWidget)),
                                this::renderWidgetTooltip));

        final ITextComponent idleAnimFieldMsg = MuseumLang.GUI_PUPPET_CONFIG_ANIM.asText();
        final int idleAnimFieldWidth = font.getStringPropertyWidth(idleAnimFieldMsg);
        final int miscY = animationsFieldY + 20 + MARGIN * 3;
        this.addButton(
                new Button(
                        left - 2,
                        miscY,
                        (WIDTH / 3) - (MARGIN / 2),
                        20,
                        MuseumLang.GUI_PUPPET_MOVE.asText(),
                        button -> mc.displayGuiScreen(new MovePuppetScreen(puppet, parent))));
        idleAnimationField =
                this.addListener(
                        new TextFieldWidget(
                                font,
                                left + (WIDTH / 3) + (MARGIN * 3) + idleAnimFieldWidth,
                                miscY,
                                WIDTH - ((WIDTH / 3) + (MARGIN * 3) + idleAnimFieldWidth) - 21,
                                20,
                                idleAnimFieldMsg));
        final IconButton selectAnimButton =
                this.addButton(
                        new IconButton(
                                idleAnimationField.x + idleAnimationField.getWidth() + 2,
                                idleAnimationField.y,
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
                                                        idleAnimationField,
                                                        () -> this.getAnimLoc().orElse(null))),
                                this::renderWidgetTooltip,
                                MuseumLang.GUI_PUPPET_CONFIG_ANIM_SELECT.asText()));

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

        textureWidget.setChangeListener(this::checkAcceptableState);

        animationsWidget.setChangeListener(
                () -> {
                    this.checkAcceptableState();
                    // Triggers the responder
                    idleAnimationField.setText(idleAnimationField.getText());
                });

        idleAnimationField.setMaxStringLength(32500);
        idleAnimationField.setResponder(
                s -> {
                    // This is needed since selectedAnimation isn't set
                    // until the done button is pressed
                    final Optional<ResourceLocation> animLoc = this.getAnimLoc();
                    if (animLoc.isPresent()) {
                        final AnimationFile animFile =
                                GeckoLibCache.getInstance().getAnimations().get(animLoc.get());
                        final boolean fileExists = animFile != null;
                        if (fileExists && (s.isEmpty() || animFile.getAnimation(s) != null)) {
                            idleAnimationField.setTextColor(TEXT_VALID);
                        } else {
                            idleAnimationField.setTextColor(TEXT_INVALID);
                        }
                        selectAnimButton.active = fileExists;
                    } else {
                        selectAnimButton.active = false;
                        idleAnimationField.setTextColor(TEXT_ERROR);
                    }
                });

        state.load();
    }

    private void checkAcceptableState() {
        doneButton.active =
                copyButton.active =
                        this.getModelLoc().isPresent()
                                && this.getTexLoc().isPresent()
                                && this.getAnimLoc().isPresent();
    }

    @Override
    public void tick() {
        modelWidget.tick();
        textureWidget.tick();
        animationsWidget.tick();
        idleAnimationField.tick();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        drawCenteredString(
                matrixStack,
                font,
                MuseumLang.GUI_PUPPET_CONFIG_TITLE.asText(title),
                width / 2,
                MARGIN * 2,
                0xFFFFFF);

        modelWidget.render(matrixStack, mouseX, mouseY, partialTicks);
        drawTitle(matrixStack, font, modelWidget);
        textureWidget.render(matrixStack, mouseX, mouseY, partialTicks);
        drawTitle(matrixStack, font, textureWidget);
        animationsWidget.render(matrixStack, mouseX, mouseY, partialTicks);
        drawTitle(matrixStack, font, animationsWidget);

        drawStringLeft(matrixStack, font, idleAnimationField, idleAnimationField.getMessage());
        idleAnimationField.render(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    protected void saveAndClose() {
        try {
            MuseumNetworking.CHANNEL.sendToServer(
                    new C2SConfigurePuppet(
                            puppet.getUniqueID(),
                            this.getModelLoc().orElse(puppet.sourceManager.model.getDirect()),
                            this.getTexLoc().orElse(puppet.sourceManager.texture.getDirect()),
                            this.getAnimLoc().orElse(puppet.sourceManager.animations.getDirect()),
                            idleAnimationField.getText()));
        } catch (ResourceLocationException e) {
            SimpleMuseum.LOGGER.error("Failed to send puppet configuration to server", e);
        }
        this.closeScreen();
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

    protected class SavedState {
        protected ResourceLocation modelState;
        protected ResourceLocation texState;
        protected ResourceLocation animsState;
        protected String idleAnimState;

        protected SavedState() {
            modelState = puppet.sourceManager.model.getDirect();
            texState = puppet.sourceManager.texture.getDirect();
            animsState = puppet.sourceManager.animations.getDirect();
            idleAnimState = puppet.animationManager.idle.getDirect();
        }

        protected void save() {
            if (modelWidget != null) modelState = modelWidget.getLocation();
            if (textureWidget != null) texState = textureWidget.getLocation();
            if (animationsWidget != null) animsState = animationsWidget.getLocation();
            if (idleAnimationField != null) idleAnimState = idleAnimationField.getText();
        }

        protected void load() {
            if (modelWidget != null) modelWidget.setLocation(modelState, true);
            if (textureWidget != null) textureWidget.setLocation(texState, true);
            if (animationsWidget != null) animationsWidget.setLocation(animsState, true);
            if (idleAnimationField != null) idleAnimationField.setText(idleAnimState);
        }
    }
}
