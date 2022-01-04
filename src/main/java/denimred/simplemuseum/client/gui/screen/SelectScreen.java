package denimred.simplemuseum.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.i18n.lang.GuiLang;

public abstract class SelectScreen<T> extends Screen {
    protected final Minecraft mc = Minecraft.getInstance();
    protected final Screen parent;
    @Nullable protected ListWidget.Entry selected;
    protected ListWidget list;
    protected EditBox search;
    protected String lastSearchText = "";

    protected SelectScreen(Screen parent, Component title) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        mc.keyboardHandler.setSendRepeatsToGui(true);

        final int margin = 10;
        final int third = width / 3;
        final int bottom = height - margin;
        final int top = (margin * 2) + font.lineHeight;
        list = new ListWidget(top, bottom, third * 2);

        final int remainingWidth = third - (margin * 3);
        final int remainingX = width - margin - remainingWidth;

        search =
                new EditBox(
                        font,
                        remainingX,
                        top + font.lineHeight + 2,
                        remainingWidth,
                        20,
                        GuiLang.SEARCH.asText());

        this.addButton(
                new Button(
                        remainingX,
                        bottom - 20 - (20 + (margin / 2)),
                        remainingWidth,
                        20,
                        CommonComponents.GUI_DONE,
                        b -> this.saveAndClose()));
        this.addButton(
                new Button(
                        remainingX,
                        bottom - 20,
                        remainingWidth,
                        20,
                        CommonComponents.GUI_CANCEL,
                        b -> this.onClose()));

        search.setResponder(
                s -> {
                    if (!s.equals(lastSearchText)) {
                        lastSearchText = s;
                        list.refreshList();
                        selected = null;
                        list.setSelected(null);
                        list.setScrollAmount(0.0D);
                    }
                });
        children.add(search);

        list.setLeftPos(margin);
        list.setRenderBackground(false);
        list.setRenderTopAndBottom(false);
        list.children().stream()
                .filter(this::isSelected)
                .findFirst()
                .ifPresent(
                        entry -> {
                            selected = entry;
                            list.setSelected(entry);
                        });
        children.add(list);
    }

    @Override
    public void tick() {
        search.tick();
        list.setSelected(selected);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        drawCenteredString(
                poseStack,
                font,
                title.plainCopy().withStyle(ChatFormatting.UNDERLINE),
                width / 2,
                10,
                0xFFFFFF);
        final double scale = mc.getWindow().getGuiScale();
        RenderSystem.enableScissor(
                (int) (list.getLeft() * scale),
                (int) ((height - list.getBottom()) * scale),
                (int) (list.getWidth() * scale),
                (int) (list.getHeight() * scale));
        list.render(poseStack, mouseX, mouseY, partialTicks);
        RenderSystem.disableScissor();
        drawString(
                poseStack,
                font,
                search.getMessage(),
                search.x,
                search.y - font.lineHeight - 2,
                0xA0A0A0);
        search.render(poseStack, mouseX, mouseY, partialTicks);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    protected void saveAndClose() {
        this.onSave();
        this.onClose();
    }

    @Override
    public void onClose() {
        mc.setScreen(parent);
    }

    @Override
    public void removed() {
        mc.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void setSelected(ListWidget.Entry entry) {
        selected = (entry == selected ? null : entry);
    }

    protected abstract void onSave();

    protected abstract boolean isSelected(ListWidget.Entry entry);

    protected abstract CompletableFuture<List<T>> getEntriesAsync();

    protected boolean matchesSearch(T value) {
        if (!lastSearchText.isEmpty()) {
            return value.toString().contains(lastSearchText);
        }
        return true;
    }

    protected class ListWidget extends ObjectSelectionList<ListWidget.Entry> {
        protected final int listWidth;
        protected boolean loading;
        protected boolean errored;

        public ListWidget(int top, int bottom, int width) {
            super(mc, width, bottom - top, top, bottom, SelectScreen.this.font.lineHeight + 6);
            this.listWidth = width;
            this.refreshList();
        }

        @Override
        protected int getScrollbarPosition() {
            return listWidth + getLeft() - 6;
        }

        @Override
        public int getRowWidth() {
            return listWidth;
        }

        public void refreshList() {
            if (!loading) {
                errored = false;
                loading = true;
                this.clearEntries();
                SelectScreen.this
                        .getEntriesAsync()
                        .exceptionally(
                                t -> {
                                    errored = true;
                                    SimpleMuseum.LOGGER.error("Exception while refreshing list", t);
                                    return Collections.emptyList();
                                })
                        .thenAccept(
                                entries -> {
                                    this.clearEntries();
                                    for (T entry : entries) {
                                        if (SelectScreen.this.matchesSearch(entry)) {
                                            this.addEntry(new ListWidget.Entry(entry));
                                        }
                                    }
                                    loading = false;
                                });
            }
        }

        @Override
        protected void renderBackground(PoseStack poseStack) {
            SelectScreen.this.fillGradient(poseStack, x0, y0, x1, y1, 0xc0101010, 0xd0101010);
        }

        @Override
        protected void renderDecorations(PoseStack poseStack, int mouseX, int mouseY) {
            if (loading || errored) {
                final Component msg;
                final float scale = 3.0F;
                final int a;
                if (loading) {
                    msg =
                            GuiLang.LOADING
                                    .asText()
                                    .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
                    a = 0x66000000;
                } else {
                    msg =
                            GuiLang.ERROR
                                    .asText()
                                    .withStyle(ChatFormatting.ITALIC, ChatFormatting.RED);
                    a = 0xFF000000;
                }

                final float width = (font.width(msg) / 2.0F) * scale;
                final float x = x0 + (this.width / 2.0F) - width;
                final float y = y0 + (height / 2.0F) - ((font.lineHeight / 2.0F) * scale);
                poseStack.pushPose();
                poseStack.scale(scale, scale, scale);
                RenderSystem.enableBlend();
                font.drawShadow(poseStack, msg, x / scale, y / scale, a | 0xFFFFFF);
                RenderSystem.disableBlend();
                poseStack.popPose();
            }
        }

        public class Entry extends ObjectSelectionList.Entry<ListWidget.Entry> {
            public final T value;

            public Entry(T value) {
                this.value = value;
            }

            @Override
            public void render(
                    PoseStack poseStack,
                    int id,
                    int top,
                    int left,
                    int width,
                    int height,
                    int mouseX,
                    int mouseY,
                    boolean hovered,
                    float partialTicks) {
                final Component name = new TextComponent(value.toString());
                font.draw(
                        poseStack,
                        Language.getInstance()
                                .getVisualOrder(
                                        FormattedText.composite(
                                                font.substrByWidth(name, listWidth))),
                        left + 3,
                        top + 3,
                        0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(
                    double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
                SelectScreen.this.setSelected(this);
                ListWidget.this.setSelected(this);
                return false;
            }
        }
    }
}
