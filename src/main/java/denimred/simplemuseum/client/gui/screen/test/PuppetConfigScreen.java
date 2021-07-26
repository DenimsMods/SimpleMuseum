package denimred.simplemuseum.client.gui.screen.test;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.gui.screen.MovePuppetScreen;
import denimred.simplemuseum.client.gui.widget.BetterButton;
import denimred.simplemuseum.client.gui.widget.ConfirmPopupWidget;
import denimred.simplemuseum.client.gui.widget.CopyPasteButtons;
import denimred.simplemuseum.client.gui.widget.IconButton;
import denimred.simplemuseum.client.gui.widget.ManagerTabs;
import denimred.simplemuseum.client.gui.widget.PuppetPreviewWidget;
import denimred.simplemuseum.client.gui.widget.WidgetList;
import denimred.simplemuseum.client.gui.widget.value.ValueWidget;
import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.client.util.LazyUtil;
import denimred.simplemuseum.client.util.ScissorUtil;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.i18n.Descriptive;
import denimred.simplemuseum.common.i18n.lang.GuiLang;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.messages.c2s.ConfigurePuppet;

public class PuppetConfigScreen extends Screen {
    public static final ResourceLocation MOVE_BUTTON_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/move_button.png");
    protected static final Minecraft MC = Minecraft.getInstance();
    protected final PuppetEntity puppet;
    protected final PuppetEntity puppetCopy;
    @Nullable protected final Screen parent;
    protected ConfirmPopupWidget confirmPopup;
    protected PuppetPreviewWidget preview;
    protected CopyPasteButtons copyPaste;
    protected IconButton move;
    protected ManagerTabs tabs;
    protected Multimap<PuppetValueManager, ValueWidget<?, ?>> valueWidgets;
    protected WidgetList<PuppetConfigScreen> list;
    protected BetterButton doneButton;
    protected BetterButton cancelButton;

    public PuppetConfigScreen(PuppetEntity puppet, @Nullable Screen parent) {
        super(puppet.getDisplayName());
        this.puppet = puppet;
        this.puppetCopy = PuppetEntity.makePreviewCopy(puppet);
        this.parent = parent;
        this.valueWidgets = LazyUtil.makeValueWidgets(this, puppetCopy);
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        minecraft.keyboardListener.enableRepeatEvents(true);
        super.init(minecraft, width, height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (IGuiEventListener iguieventlistener : this.getEventListeners()) {
            if (iguieventlistener.mouseClicked(mouseX, mouseY, button)) {
                this.setListener(iguieventlistener);
                this.setDragging(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(
            double mouseX, double mouseY, int button, double dragX, double dragY) {
        final IGuiEventListener listener = this.getListener();
        return listener != null
                && this.isDragging()
                && listener.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_SPACE && !confirmPopup.visible) {
            //            confirmPopup
            //                    .title("Possible Side Effects")
            //                    .description(
            //                            "When changing the animations source, resetting/clearing
            // the following values is highly recommended to prevent side effects:")
            //                    .extra(
            //                            "   - Idle Animation",
            //                            "   - Idle Animation (Sneaking)",
            //                            "   - Walking Animation",
            //                            "   - Walking Animation (Sneaking)",
            //                            "   - Sprinting Animation",
            //                            "   - Death Animation",
            //                            "   - Death Animation Length")
            //                    .challenge("Should this be done automatically?")
            //                    .yes(() -> System.out.println("Cool dude"))
            //                    .no(() -> System.out.println("Alright that's okay"))
            //                    .display(width / 3);
            //            return true;
        }
        return false;
    }

    @Override
    protected void init() {
        // Order is important to make sure clicks are consumed correctly
        confirmPopup = this.addButton(new ConfirmPopupWidget(width / 2, height / 2, font));
        confirmPopup.setBlitOffset(1000);
        final int third = width / 3;
        preview =
                this.addButton(
                        new PuppetPreviewWidget(
                                this,
                                third * 2,
                                0,
                                third,
                                height,
                                puppetCopy,
                                this::renderWidgetTooltip));
        list = this.addButton(new WidgetList<>(this, 2, 24, third * 2 - 4, height - 48));
        copyPaste = this.addButton(new CopyPasteButtons(this, 2, 2));
        move =
                this.addButton(
                        new IconButton(
                                48,
                                2,
                                20,
                                20,
                                MOVE_BUTTON_TEXTURE,
                                0,
                                0,
                                64,
                                32,
                                20,
                                button -> MC.displayGuiScreen(new MovePuppetScreen(puppet, this)),
                                this::renderWidgetTooltip,
                                GuiLang.PUPPET_MOVE.asText()));
        tabs =
                this.addButton(
                        new ManagerTabs(
                                this,
                                third - (20 * puppetCopy.getManagers().size()) / 2,
                                2,
                                puppetCopy.getManagers(),
                                m -> {
                                    list.clear();
                                    for (ValueWidget<?, ?> widget : valueWidgets.get(m)) {
                                        widget.detectChanges();
                                        list.add(widget);
                                    }
                                }));
        final int bw = (third * 2) / 3;
        doneButton =
                this.addButton(
                        new BetterButton(
                                third - bw,
                                height - 22,
                                bw,
                                20,
                                new StringTextComponent("Done"),
                                b -> this.saveAndClose()));
        cancelButton =
                this.addButton(
                        new BetterButton(
                                third,
                                height - 22,
                                bw,
                                20,
                                new StringTextComponent("Cancel"),
                                b -> this.closeScreen()));
    }

    @Override
    public boolean changeFocus(boolean focus) {
        // TODO: This is busted
        return false;
    }

    @Override
    public void tick() {
        list.tick();
    }

    public void renderWidgetTooltip(
            Widget widget, MatrixStack matrixStack, int mouseX, int mouseY) {
        if (widget instanceof Descriptive) {
            this.renderDescriptiveTooltip((Descriptive) widget, matrixStack, mouseX, mouseY);
        } else {
            this.renderTooltip(matrixStack, widget.getMessage(), mouseX, mouseY);
        }
    }

    @Override
    public void renderWrappedToolTip(
            MatrixStack matrixStack,
            List<? extends ITextProperties> lines,
            int mouseX,
            int mouseY,
            FontRenderer font) {
        ScissorUtil.push();
        GuiUtils.drawHoveringText(matrixStack, lines, mouseX, mouseY, width, height, -1, font);
        ScissorUtil.pop();
    }

    public void renderDescriptiveTooltip(
            Descriptive desc, MatrixStack matrixStack, int mouseX, int mouseY) {
        final List<ITextComponent> lines = new ArrayList<>();
        lines.add(desc.getTitle());
        if (!desc.hideDescription() || ClientUtil.hasKeyDown(MC.gameSettings.keyBindSneak)) {
            lines.addAll(desc.getDescription());
        } else {
            lines.add(
                    new TranslationTextComponent(
                                    "Hold %s for details",
                                    new TranslationTextComponent(
                                                    MC.gameSettings.keyBindSneak
                                                            .getTranslationKey())
                                            .mergeStyle(TextFormatting.GOLD))
                            .mergeStyle(TextFormatting.GRAY));
        }
        if (MC.gameSettings.advancedItemTooltips) {
            lines.addAll(desc.getAdvancedDescription());
        }
        ScissorUtil.push();
        GuiUtils.drawHoveringText(matrixStack, lines, mouseX, mouseY, width, height, 200, font);
        ScissorUtil.pop();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(matrixStack);
        puppetCopy.ticksExisted = puppet.ticksExisted;
        preview.render(matrixStack, mouseX, mouseY, partialTicks);
        final float fullscreenness = preview.getFullscreenness();
        if (fullscreenness < 1.0F) {
            final boolean shouldScissor = fullscreenness > 0.0F;
            if (shouldScissor) {
                ScissorUtil.start(0, 0, width - preview.getWidth(), height);
            }

            doneButton.render(matrixStack, mouseX, mouseY, partialTicks);
            cancelButton.render(matrixStack, mouseX, mouseY, partialTicks);
            list.render(matrixStack, mouseX, mouseY, partialTicks);
            copyPaste.render(matrixStack, mouseX, mouseY, partialTicks);
            tabs.render(matrixStack, mouseX, mouseY, partialTicks);
            move.render(matrixStack, mouseX, mouseY, partialTicks);

            if (shouldScissor) {
                ScissorUtil.stop();
            }
        }
        confirmPopup.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    public void saveAndClose() {
        final ConfigurePuppet msg = ConfigurePuppet.transplant(puppetCopy, puppet);
        if (!msg.isEmpty()) {
            MuseumNetworking.CHANNEL.sendToServer(msg);
        }
        this.closeScreen();
    }

    @Override
    public void closeScreen() {
        MC.displayGuiScreen(parent);
    }

    @Override
    public void onClose() {
        MC.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public ResourceLocation getAnimationFile() {
        return puppetCopy.sourceManager.animations.get();
    }

    public void copy() {
        final CompoundNBT tag = new CompoundNBT();
        puppetCopy.writeModTag(tag);
        ClientUtil.MC.keyboardListener.setClipboardString(tag.toString());
    }

    public void paste() {
        try {
            final String clipboard = ClientUtil.MC.keyboardListener.getClipboardString();
            puppetCopy.readModTag(JsonToNBT.getTagFromJson(clipboard));
            valueWidgets.values().forEach(ValueWidget::detectAndSync);
        } catch (CommandSyntaxException ignored) {
            // bluh
        }
    }
}
