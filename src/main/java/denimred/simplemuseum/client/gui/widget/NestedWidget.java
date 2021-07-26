package denimred.simplemuseum.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

public class NestedWidget extends Widget implements ITickingWidget {
    protected final List<Widget> children = new ArrayList<>();
    protected int maxWidth = Integer.MAX_VALUE;

    public NestedWidget(int x, int y, int width, int height, ITextComponent title) {
        super(x, y, width, height, title);
    }

    protected <T extends Widget> T addChild(T child) {
        return this.addChild(child, false);
    }

    protected <T extends Widget> T addChild(T child, boolean reverseOrder) {
        if (reverseOrder) {
            children.add(0, child);
        } else {
            children.add(child);
        }
        return child;
    }

    protected void removeChild(Widget child) {
        children.remove(child);
    }

    protected void clearChildren() {
        children.clear();
    }

    protected void swapChild(Widget oldChild, Widget newChild) {
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
        for (Widget widget : children) {
            widget.setAlpha(alpha);
        }
    }

    @Override
    public void playDownSound(SoundHandler handler) {
        // no-op
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (visible && active) {
            boolean b = false;
            for (Widget widget : children) {
                b = widget.keyPressed(keyCode, scanCode, modifiers);
            }
            return b || super.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (visible && active) {
            for (Widget widget : children) {
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
        // TODO: This is stupid
        for (Widget child : children) {
            if (child instanceof TextFieldWidget) {
                ((TextFieldWidget) child).setFocused2(false);
            }
        }
        if (this.isMouseOver(mouseX, mouseY)) {
            boolean b = false;
            for (Widget widget : children) {
                if (widget.mouseClicked(mouseX, mouseY, mouseButton)) {
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
            for (Widget widget : children) {
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
            for (Widget widget : children) {
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
        if (super.isMouseOver(mouseX, mouseY)) {
            for (Widget widget : children) {
                if (widget.isMouseOver(mouseX, mouseY)) {
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        for (Widget child : children) {
            child.render(matrixStack, mouseX, mouseY, partialTicks);
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
        for (Widget child : children) {
            if (child instanceof ITickingWidget) {
                ((ITickingWidget) child).tick();
            }
        }
    }
}
