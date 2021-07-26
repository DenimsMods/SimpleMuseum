package denimred.simplemuseum.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

// Forge pls
public class BetterButton extends ExtendedButton {
    private final ITooltip tooltip;

    public BetterButton(
            int x, int y, int width, int height, ITextComponent text, IPressable pressable) {
        this(x, y, width, height, text, pressable, EMPTY_TOOLTIP);
    }

    public BetterButton(
            int x,
            int y,
            int width,
            int height,
            ITextComponent text,
            IPressable pressable,
            ITooltip tooltip) {
        super(x, y, width, height, text, pressable);
        this.tooltip = tooltip;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false; // todo ugh
    }

    @Override
    public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
        tooltip.onTooltip(this, matrixStack, mouseX, mouseY);
    }
}
