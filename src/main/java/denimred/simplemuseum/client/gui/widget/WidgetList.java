package denimred.simplemuseum.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.awt.Color;

import denimred.simplemuseum.client.util.ScissorUtil;

public class WidgetList<T extends Screen> extends NestedWidget {
    public final T parent;
    protected int totalHeight;
    protected int scrollPos;
    protected int scrollBarWidth = 8;
    protected boolean scrollBarLeft;
    protected boolean scrolling;

    public WidgetList(T parent, int x, int y, int width, int height) {
        this(parent, x, y, width, height, StringTextComponent.EMPTY);
    }

    public WidgetList(T parent, int x, int y, int width, int height, ITextComponent title) {
        super(x, y, width, height, title);
        this.parent = parent;
    }

    public void add(Widget widget) {
        this.addChild(widget);
        final int realWidth = width - scrollBarWidth;
        widget.setWidth(realWidth);
        final int widgetX = x + realWidth / 2 - widget.getWidth() / 2;
        if (widget instanceof NestedWidget) {
            final NestedWidget nested = (NestedWidget) widget;
            nested.setX(widgetX);
            nested.setY(y + totalHeight);
        } else {
            widget.x = widgetX;
            widget.y = y + totalHeight;
        }
        totalHeight += widget.getHeight();
    }

    public void remove(int i) {
        if (i > 0 && i < children.size()) {
            this.remove(children.get(i));
        }
    }

    public void remove(Widget widget) {
        if (children.contains(widget)) {
            this.removeChild(widget);
            totalHeight -= widget.getHeight();
        }
    }

    public void clear() {
        this.scrollTo(0.0D);
        this.clearChildren();
        totalHeight = 0;
    }

    public void scrollTo(double pos) {
        final double percent = (pos - y) / height;
        this.setScrollPos((int) (this.getMaxScroll() * percent));
    }

    public void setScrollPos(int pos) {
        final int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            final int last = scrollPos;
            scrollPos = pos < 0 ? 0 : Math.min(pos, maxScroll);
            if (scrollPos != last) {
                final int diff = scrollPos - last;
                for (Widget child : children) {
                    if (child instanceof NestedWidget) {
                        ((NestedWidget) child).setY(child.y - diff);
                    } else {
                        child.y -= diff;
                    }
                    child.visible = child.y <= y + height && child.y + child.getHeight() >= y;
                }
            }
        }
    }

    public int getMaxScroll() {
        return totalHeight - height;
    }

    public boolean isScrollBarLeft() {
        return scrollBarLeft;
    }

    public void setScrollBarLeft(boolean scrollBarLeft) {
        this.scrollBarLeft = scrollBarLeft;
        for (Widget child : children) {
            final int childX = scrollBarLeft ? x + scrollBarWidth : x;
            if (child instanceof NestedWidget) {
                ((NestedWidget) child).setX(childX);
            } else {
                child.x = childX;
            }
        }
    }

    public int getScrollBarWidth() {
        return scrollBarWidth;
    }

    public void setScrollBarWidth(int scrollBarWidth) {
        this.scrollBarWidth = scrollBarWidth;
        for (Widget child : children) {
            child.setWidth(width - scrollBarWidth);
            if (scrollBarLeft) {
                if (child instanceof NestedWidget) {
                    ((NestedWidget) child).setX(x + scrollBarWidth);
                } else {
                    child.x = x + scrollBarWidth;
                }
            }
        }
    }

    public int getScrollBarHeight() {
        if (totalHeight > height) {
            final double factor = (double) height / totalHeight;
            return MathHelper.clamp((int) (factor * height), 40, height);
        } else {
            return height;
        }
    }

    public int getScrollBarY() {
        final float adjustedHeight = height - this.getScrollBarHeight();
        final float scrollPercent = (float) scrollPos / (float) this.getMaxScroll();
        return (int) (y + scrollPercent * adjustedHeight);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (visible && active) {
            if (this.isValidClickButton(mouseButton)) {
                scrolling =
                        scrollBarLeft
                                ? mouseX >= x && mouseX <= x + scrollBarWidth
                                : mouseX >= x + width - scrollBarWidth && mouseX <= x + width;
                final int barY = this.getScrollBarY();
                if (scrolling && (mouseY < barY || mouseY > barY + this.getScrollBarHeight())) {
                    this.scrollTo(mouseY);
                }
            }
            return scrolling || super.mouseClicked(mouseX, mouseY, mouseButton);
        }
        scrolling = false;
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (scrolling) {
            scrolling = false;
        }
        return super.mouseReleased(mouseX, mouseY, mouseButton) || !scrolling;
    }

    @Override
    public boolean mouseDragged(
            double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (visible && active && scrolling) {
            final int maxScroll = this.getMaxScroll();
            if (mouseY < y || maxScroll <= 0) {
                this.setScrollPos(0);
            } else if (mouseY > y + height) {
                this.setScrollPos(maxScroll);
            } else {
                this.scrollTo(mouseY);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        final double averageHeight = (double) totalHeight / children.size();
        this.setScrollPos((int) (scrollPos - delta * averageHeight / 3.0D));
        return true;
    }

    @Override
    public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        fill(matrixStack, x, y, x + width, y + height, 0x66000000);
        this.renderScrollBar(matrixStack);
        ScissorUtil.start(
                scrollBarLeft ? x + scrollBarWidth : x, y, width - scrollBarWidth, height);
        for (int i = 0, size = children.size(); i < size; i++) {
            final Widget child = children.get(i);
            if (this.isWidgetWithin(child)) {
                final int color = Color.HSBtoRGB(0.0F, 0.0F, i % 2 == 0 ? 0.1F : 0.2F) & 0x66FFFFFF;
                fill(matrixStack, x, child.y, x + width, child.y + child.getHeight(), color);
            }
        }
        super.renderWidget(matrixStack, mouseX, mouseY, partialTicks);
        ScissorUtil.stop();
    }

    protected boolean isWidgetWithin(Widget widget) {
        return widget.x <= x + width
                && widget.x + widget.getWidth() >= x
                && widget.y <= y + height
                && widget.y + widget.getHeight() >= y;
    }

    protected void renderScrollBar(MatrixStack matrixStack) {
        final int minX = scrollBarLeft ? x : x + width - scrollBarWidth;
        final int maxX = minX + scrollBarWidth;
        final int minY = y;
        final int maxY = y + height;
        fill(matrixStack, minX, minY, maxX, maxY, 0x55000000);
        final int barHeight = this.getScrollBarHeight();
        final int barY = this.getScrollBarY();
        fill(matrixStack, minX, barY, maxX, barY + barHeight, 0xFFDDDDDD);
        fill(matrixStack, minX + 1, barY + 1, maxX, barY + barHeight, 0xFF666666);
        fill(matrixStack, minX + 1, barY + 1, maxX - 1, barY + barHeight - 1, 0xFFAAAAAA);
        final int lines = barHeight / 6;
        for (int i = 0; i < lines; i++) {
            final int y = barY + barHeight / 2 - lines + 2 * i;
            hLine(matrixStack, minX, maxX - 1, y, 0x44000000);
        }
    }
}
