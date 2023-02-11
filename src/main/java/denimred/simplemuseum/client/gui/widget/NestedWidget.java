package denimred.simplemuseum.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class NestedWidget extends AbstractWidget implements ITickingWidget {
    protected final List<AbstractWidget> children = new ArrayList<>();
    protected int maxWidth = Integer.MAX_VALUE;

    public NestedWidget(int x, int y, int width, int height, Component title) {
        super(x, y, width, height, title);
    }

    protected <T extends AbstractWidget> T addChild(T child) {
        return this.addChild(child, false);
    }

    protected <T extends AbstractWidget> T addChild(T child, boolean reverseOrder) {
        if (reverseOrder) {
            children.add(0, child);
        } else {
            children.add(child);
        }
        return child;
    }

    protected void removeChild(AbstractWidget child) {
        children.remove(child);
    }

    protected void clearChildren() {
        children.clear();
    }

    protected void swapChild(AbstractWidget oldChild, AbstractWidget newChild) {
        children.remove(oldChild);
        newChild.setAlpha(oldChild.alpha);
        children.add(newChild);
    }

    public void setX(int x) {
        this.x = x;
        this.recalculateChildren();
    }

    public void setY(int y) {
        this.y = y;
        this.recalculateChildren();
    }

    @Override
    public void setWidth(int width) {
        this.width = Math.min(width, maxWidth);
        this.recalculateChildren();
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
        this.recalculateChildren();
    }

    protected void recalculateChildren() {}

    @Override
    public void setAlpha(float alpha) {
        this.alpha = alpha;
        for (AbstractWidget child : children) {
            child.setAlpha(alpha);
        }
    }

    @Override
    public void playDownSound(SoundManager manager) {
        // no-op
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (visible && active) {
            boolean b = false;
            for (AbstractWidget child : children) {
                b = child.keyPressed(keyCode, scanCode, modifiers);
            }
            return b || super.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (visible && active) {
            for (AbstractWidget child : children) {
                if (child.charTyped(codePoint, modifiers)) {
                    return true;
                }
            }
            return super.charTyped(codePoint, modifiers);
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        // TODO: This is stupid
        for (AbstractWidget child : children) {
            if (child instanceof EditBox) {
                ((EditBox) child).setFocus(false);
            }
        }
        if (this.isMouseOver(mouseX, mouseY)) {
            boolean b = false;
            for (AbstractWidget child : children) {
                if (child.mouseClicked(mouseX, mouseY, mouseButton)) {
                    b = true;
                }
            }
            return b || super.mouseClicked(mouseX, mouseY, mouseButton);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (this.isMouseOver(mouseX, mouseY)) {
            boolean b = false;
            for (AbstractWidget child : children) {
                if (child.mouseReleased(mouseX, mouseY, mouseButton)) {
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
            for (AbstractWidget child : children) {
                if (child.changeFocus(focus)) {
                    return true;
                }
            }
            return super.changeFocus(focus);
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (super.isMouseOver(mouseX, mouseY)) {
            for (AbstractWidget child : children) {
                if (child.isMouseOver(mouseX, mouseY)) {
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        for (AbstractWidget child : children) {
            child.render(poseStack, mouseX, mouseY, partialTicks);
        }
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        this.setWidth(width);
    }

    @Override
    public void tick() {
        for (AbstractWidget child : children) {
            if (child instanceof ITickingWidget) {
                ((ITickingWidget) child).tick();
            }
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        // no-op
    }
}
