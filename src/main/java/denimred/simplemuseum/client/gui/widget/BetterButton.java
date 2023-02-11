package denimred.simplemuseum.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ExtendedButton;

// Forge pls
public class BetterButton extends ExtendedButton {
    private final OnTooltip tooltip;

    public BetterButton(int x, int y, int width, int height, Component text, OnPress press) {
        this(x, y, width, height, text, press, NO_TOOLTIP);
    }

    public BetterButton(
            int x, int y, int width, int height, Component text, OnPress press, OnTooltip tooltip) {
        super(x, y, width, height, text, press);
        this.tooltip = tooltip;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false; // todo ugh
    }

    @Override
    public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
        tooltip.onTooltip(this, poseStack, mouseX, mouseY);
    }
}
