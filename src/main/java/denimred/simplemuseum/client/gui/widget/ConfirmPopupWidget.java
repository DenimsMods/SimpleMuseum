package denimred.simplemuseum.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import denimred.simplemuseum.client.gui.widget.LabelWidget.AnchorX;
import denimred.simplemuseum.client.gui.widget.LabelWidget.AnchorY;
import denimred.simplemuseum.common.util.MathUtil;

public class ConfirmPopupWidget extends AbstractWidget {
    public static final int MARGIN = 6;
    protected static final int BORDER_WIDTH = 2;
    protected static final int BUTTON_SIZE = 20;
    protected final int originX;
    protected final int originY;
    protected final AbstractWidget[] widgets;
    protected final LabelWidget titleLabel;
    protected final LabelWidget descriptionLabel;
    protected final LabelWidget extraLabel;
    protected final LabelWidget challengeLabel;
    protected final Button yesButton;
    protected final Button noButton;
    protected Runnable yesAction = () -> {};
    protected Runnable noAction = () -> {};

    public ConfirmPopupWidget(int x, int y, Font font) {
        super(x, y, 100, 100, TextComponent.EMPTY);
        originX = x;
        originY = y;
        widgets =
                new AbstractWidget[] {
                    yesButton =
                            new ExtendedButton(
                                    0,
                                    0,
                                    BUTTON_SIZE,
                                    BUTTON_SIZE,
                                    TextComponent.EMPTY,
                                    this::onYes),
                    noButton =
                            new ExtendedButton(
                                    0,
                                    0,
                                    BUTTON_SIZE,
                                    BUTTON_SIZE,
                                    TextComponent.EMPTY,
                                    this::onNo),
                    titleLabel = new LabelWidget(x, y, font, AnchorX.CENTER, AnchorY.TOP),
                    descriptionLabel = new LabelWidget(x, y, font, AnchorX.LEFT, AnchorY.TOP),
                    extraLabel = new LabelWidget(x, y, font, AnchorX.LEFT, AnchorY.TOP),
                    challengeLabel = new LabelWidget(x, y, font, AnchorX.CENTER, AnchorY.BOTTOM)
                };
        visible = false;
        active = false;
    }

    private void rebuild(int wrapWidth) {
        if (wrapWidth > 0) {
            width = wrapWidth;
            final int innerWrap = wrapWidth - BORDER_WIDTH * 4;
            titleLabel.wrap(innerWrap);
            descriptionLabel.wrap(innerWrap);
            extraLabel.wrap(innerWrap);
            challengeLabel.wrap(innerWrap);
        } else {
            width =
                    MathUtil.max(
                                    titleLabel.getWidth(),
                                    descriptionLabel.getWidth(),
                                    extraLabel.getWidth(),
                                    challengeLabel.getWidth())
                            + BORDER_WIDTH * 4;
        }
        height =
                titleLabel.getHeight()
                        + descriptionLabel.getHeight()
                        + extraLabel.getHeight()
                        + challengeLabel.getHeight()
                        + BUTTON_SIZE
                        + BORDER_WIDTH * 4
                        + MARGIN * 3;
        x = originX - width / 2;
        y = originY - height / 2;
        final int top = y + BORDER_WIDTH * 2;
        final int left = x + BORDER_WIDTH;
        final int bottom = y + height - BORDER_WIDTH - BUTTON_SIZE;
        titleLabel.y = top;
        descriptionLabel.x = left + BORDER_WIDTH;
        descriptionLabel.y = titleLabel.y + titleLabel.getHeight() + MARGIN;
        extraLabel.x = descriptionLabel.x;
        extraLabel.y = descriptionLabel.y + descriptionLabel.getHeight() + MARGIN;
        challengeLabel.y = bottom - BORDER_WIDTH;
        yesButton.x = left;
        yesButton.y = bottom;
        final int buttonWidth = Mth.floor((width - BORDER_WIDTH * 2) / 2.0F);
        yesButton.setWidth(buttonWidth);
        noButton.x = x + width - BORDER_WIDTH - buttonWidth;
        noButton.y = bottom;
        noButton.setWidth(buttonWidth);
    }

    private void onYes(Button yesButton) {
        if (visible && active) {
            yesAction.run();
            visible = false;
            active = false;
        }
    }

    private void onNo(Button noButton) {
        if (visible && active) {
            noAction.run();
            visible = false;
            active = false;
        }
    }

    public ConfirmPopupWidget title(String title) {
        return this.title(new TextComponent(title).withStyle(ChatFormatting.UNDERLINE));
    }

    public ConfirmPopupWidget title(Component title) {
        this.message = title;
        this.titleLabel.setTexts(title);
        return this;
    }

    public ConfirmPopupWidget description(String... texts) {
        final List<Component> list = new ArrayList<>();
        for (String text : texts) {
            list.add(new TextComponent(text));
        }
        return this.description(list);
    }

    public ConfirmPopupWidget description(Component... texts) {
        return this.description(Arrays.asList(texts));
    }

    public ConfirmPopupWidget description(List<? extends Component> texts) {
        this.descriptionLabel.setTexts(texts);
        return this;
    }

    public ConfirmPopupWidget extra(String... texts) {
        final List<Component> list = new ArrayList<>();
        for (String text : texts) {
            list.add(new TextComponent(text));
        }
        return this.extra(list);
    }

    public ConfirmPopupWidget extra(Component... texts) {
        return this.extra(Arrays.asList(texts));
    }

    public ConfirmPopupWidget extra(List<? extends Component> texts) {
        this.extraLabel.setTexts(texts);
        return this;
    }

    public ConfirmPopupWidget challenge(String title) {
        return this.challenge(new TextComponent(title));
    }

    public ConfirmPopupWidget challenge(Component title) {
        this.message = title;
        this.challengeLabel.setTexts(title);
        return this;
    }

    public ConfirmPopupWidget yes(Runnable action) {
        return this.yes("Yes", action);
    }

    public ConfirmPopupWidget yes(String title, Runnable action) {
        return this.yes(new TextComponent(title), action);
    }

    public ConfirmPopupWidget yes(Component title, Runnable action) {
        yesButton.setMessage(title);
        this.yesAction = action;
        return this;
    }

    public ConfirmPopupWidget no(Runnable action) {
        return this.no("No", action);
    }

    public ConfirmPopupWidget no(String title, Runnable action) {
        return this.no(new TextComponent(title), action);
    }

    public ConfirmPopupWidget no(Component title, Runnable action) {
        noButton.setMessage(title);
        this.noAction = action;
        return this;
    }

    public void display() {
        this.display(0);
    }

    public void display(int wrapWidth) {
        this.rebuild(wrapWidth);
        this.visible = true;
        this.active = true;
    }

    @Override
    public void playDownSound(SoundManager manager) {
        // no-op
    }

    @SuppressWarnings("deprecation")
    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        fillGradient(poseStack, x, y, x + width, y + height, 0x66FFFFFF, 0x66FFFFFF);
        fillGradient(
                poseStack,
                x + BORDER_WIDTH,
                y + BORDER_WIDTH,
                x + width - BORDER_WIDTH,
                y + height - BORDER_WIDTH,
                0x66000000,
                0xCC000000);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, this.getBlitOffset());
        for (AbstractWidget widget : widgets) {
            widget.render(poseStack, mouseX, mouseY, partialTicks);
        }
        poseStack.popPose();
        RenderSystem.disableBlend();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (visible && active) {
            for (AbstractWidget widget : widgets) {
                if (widget.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (visible && active) {
            for (AbstractWidget widget : widgets) {
                if (widget.charTyped(codePoint, modifiers)) {
                    return true;
                }
            }
            return super.charTyped(codePoint, modifiers);
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (visible && active) {
            for (AbstractWidget widget : widgets) {
                if (widget.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (visible && active) {
            boolean b = false;
            for (AbstractWidget widget : widgets) {
                if (widget.mouseReleased(mouseX, mouseY, mouseButton)) {
                    b = true;
                }
            }
            return b || super.mouseReleased(mouseX, mouseY, mouseButton);
        }
        return false;
    }

    @Override
    public boolean changeFocus(boolean focus) {
        if (visible && active) {
            for (AbstractWidget widget : widgets) {
                if (widget.changeFocus(focus)) {
                    return true;
                }
            }
            return super.changeFocus(focus);
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (visible && active) {
            for (AbstractWidget widget : widgets) {
                if (widget.isMouseOver(mouseX, mouseY)) {
                    return true;
                }
            }
            return super.isMouseOver(mouseX, mouseY);
        }
        return false;
    }
}
