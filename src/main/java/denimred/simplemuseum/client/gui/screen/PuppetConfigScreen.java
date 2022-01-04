package denimred.simplemuseum.client.gui.screen;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.client.gui.GuiUtils;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;
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
    protected final Multimap<PuppetValueManager, ValueWidget<?, ?>> valueWidgets;
    protected ConfirmPopupWidget confirmPopup;
    protected PuppetPreviewWidget preview;
    protected CopyPasteButtons copyPaste;
    protected IconButton move;
    protected ManagerTabs tabs;
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
        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        super.init(minecraft, width, height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener child : this.children()) {
            if (child.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(child);
                this.setDragging(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(
            double mouseX, double mouseY, int button, double dragX, double dragY) {
        final GuiEventListener focused = this.getFocused();
        return focused != null
                && this.isDragging()
                && focused.mouseDragged(mouseX, mouseY, button, dragX, dragY);
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
                                button -> MC.setScreen(new MovePuppetScreen(puppet, this)),
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
                                new TextComponent("Done"),
                                b -> this.saveAndClose()));
        cancelButton =
                this.addButton(
                        new BetterButton(
                                third,
                                height - 22,
                                bw,
                                20,
                                new TextComponent("Cancel"),
                                b -> this.onClose()));
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
            AbstractWidget widget, PoseStack poseStack, int mouseX, int mouseY) {
        if (widget instanceof Descriptive) {
            this.renderDescriptiveTooltip((Descriptive) widget, poseStack, mouseX, mouseY);
        } else {
            this.renderTooltip(poseStack, widget.getMessage(), mouseX, mouseY);
        }
    }

    @Override
    public void renderWrappedToolTip(
            PoseStack poseStack,
            List<? extends FormattedText> lines,
            int mouseX,
            int mouseY,
            Font font) {
        ScissorUtil.push();
        GuiUtils.drawHoveringText(poseStack, lines, mouseX, mouseY, width, height, -1, font);
        ScissorUtil.pop();
    }

    public void renderDescriptiveTooltip(
            Descriptive desc, PoseStack poseStack, int mouseX, int mouseY) {
        final List<Component> lines = new ArrayList<>();
        lines.add(desc.getTitle());
        if (!desc.hideDescription() || ClientUtil.hasKeyDown(MC.options.keyShift)) {
            lines.addAll(desc.getDescription());
        } else {
            lines.add(
                    new TranslatableComponent(
                                    "Hold %s for details",
                                    new TranslatableComponent(MC.options.keyShift.saveString())
                                            .withStyle(ChatFormatting.GOLD))
                            .withStyle(ChatFormatting.GRAY));
        }
        if (MC.options.advancedItemTooltips) {
            lines.addAll(desc.getAdvancedDescription());
        }
        ScissorUtil.push();
        GuiUtils.drawHoveringText(poseStack, lines, mouseX, mouseY, width, height, 200, font);
        ScissorUtil.pop();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(poseStack);
        puppetCopy.tickCount = puppet.tickCount;
        preview.render(poseStack, mouseX, mouseY, partialTicks);
        final float fullscreenness = preview.getFullscreenness();
        if (fullscreenness < 1.0F) {
            final boolean shouldScissor = fullscreenness > 0.0F;
            if (shouldScissor) {
                ScissorUtil.start(0, 0, width - preview.getWidth(), height);
            }

            doneButton.render(poseStack, mouseX, mouseY, partialTicks);
            cancelButton.render(poseStack, mouseX, mouseY, partialTicks);
            list.render(poseStack, mouseX, mouseY, partialTicks);
            copyPaste.render(poseStack, mouseX, mouseY, partialTicks);
            tabs.render(poseStack, mouseX, mouseY, partialTicks);
            move.render(poseStack, mouseX, mouseY, partialTicks);

            if (shouldScissor) {
                ScissorUtil.stop();
            }
        }
        confirmPopup.render(poseStack, mouseX, mouseY, partialTicks);
    }

    public void saveAndClose() {
        final ConfigurePuppet msg = ConfigurePuppet.transplant(puppetCopy, puppet);
        if (!msg.isEmpty()) {
            MuseumNetworking.CHANNEL.sendToServer(msg);
        }
        this.onClose();
    }

    @Override
    public void onClose() {
        MC.setScreen(parent);
    }

    @Override
    public void removed() {
        MC.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public ResourceLocation getAnimationFile() {
        return puppetCopy.sourceManager.animations.get();
    }

    public void copy() {
        final CompoundTag tag = new CompoundTag();
        puppetCopy.writeModTag(tag);
        ClientUtil.MC.keyboardHandler.setClipboard(tag.toString());
    }

    public void paste() {
        try {
            final String clipboard = ClientUtil.MC.keyboardHandler.getClipboard();
            puppetCopy.readModTag(TagParser.parseTag(clipboard));
            valueWidgets.values().forEach(ValueWidget::detectAndSync);
        } catch (CommandSyntaxException ignored) {
            // bluh
        }
    }
}
