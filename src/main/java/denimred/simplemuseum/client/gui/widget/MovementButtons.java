package denimred.simplemuseum.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Function;

import javax.annotation.Nonnull;

import denimred.simplemuseum.SimpleMuseum;

public class MovementButtons extends Widget {
    public static final int ROTATE_COUNTER_CLOCKWISE = 0;
    public static final int MOVE_FORWARD = 1;
    public static final int ROTATE_CLOCKWISE = 2;
    public static final int MOVE_LEFT = 3;
    public static final int CENTER = 4;
    public static final int MOVE_RIGHT = 5;
    public static final int MOVE_UP = 6;
    public static final int MOVE_BACK = 7;
    public static final int MOVE_DOWN = 8;
    private static final ResourceLocation MOVEMENT_BUTTONS_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/movement_buttons.png");
    private static final int SIZE = 20;
    private static final int DIM = 3;
    private final MoveButton[] buttons = new MoveButton[DIM * DIM];

    public MovementButtons(
            int x,
            int y,
            ITextComponent title,
            Function<Integer, ITextComponent> titleMapper,
            MultiPressable press,
            Button.ITooltip tooltip) {
        super(x, y, SIZE * DIM, SIZE * DIM, title);
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = makeButton(x, y, titleMapper, press, tooltip, i);
        }
    }

    public static IFormattableTextComponent getName(int index) {
        // TODO: Localize
        switch (index) {
            case ROTATE_COUNTER_CLOCKWISE:
                return new StringTextComponent("Rotate Counter Clockwise");
            case MOVE_FORWARD:
                return new StringTextComponent("Move Forward");
            case ROTATE_CLOCKWISE:
                return new StringTextComponent("Rotate Clockwise");
            case MOVE_LEFT:
                return new StringTextComponent("Move Left");
            case CENTER:
                return new StringTextComponent("Center on Block");
            case MOVE_RIGHT:
                return new StringTextComponent("Move right");
            case MOVE_UP:
                return new StringTextComponent("Move Up");
            case MOVE_BACK:
                return new StringTextComponent("Move Back");
            case MOVE_DOWN:
                return new StringTextComponent("Move Down");
        }
        return new StringTextComponent("Unknown button index: " + index);
    }

    @Nonnull
    private static MoveButton makeButton(
            int x,
            int y,
            Function<Integer, ITextComponent> titleMapper,
            MultiPressable press,
            Button.ITooltip tooltip,
            int i) {
        final int col = i % DIM;
        final int row = (i - col) / DIM;
        return new MoveButton(x, y, col, row, titleMapper.apply(i), b -> press.press(i), tooltip);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        for (final MoveButton button : buttons) {
            button.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (MoveButton button : buttons) {
            if (button.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (MoveButton button : buttons) {
            if (button.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        for (MoveButton button : buttons) {
            if (button.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean changeFocus(boolean focus) {
        for (MoveButton button : buttons) {
            if (button.changeFocus(focus)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for (MoveButton button : buttons) {
            if (button.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface MultiPressable {
        void press(int index);
    }

    private static final class MoveButton extends Button {
        private final int col;
        private final int row;

        public MoveButton(
                int x,
                int y,
                int col,
                int row,
                ITextComponent title,
                IPressable pressedAction,
                ITooltip onTooltip) {
            super(x + (SIZE * col), y + (SIZE * row), SIZE, SIZE, title, pressedAction, onTooltip);
            this.col = col;
            this.row = row;
        }

        @SuppressWarnings("deprecation") // >:I Mojang
        @Override
        public void renderButton(
                MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.getTextureManager().bindTexture(MOVEMENT_BUTTONS_TEXTURE);
            int yTex = SIZE * row;
            if (!active) {
                yTex += SIZE * (DIM * 3);
            } else if (this.isHovered()) {
                yTex += SIZE * DIM;
            }

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
            RenderSystem.enableDepthTest();
            blit(matrixStack, x, y, SIZE * col, yTex, width, height, 64, 256);
            if (this.isHovered()) {
                this.renderToolTip(matrixStack, mouseX, mouseY);
            }
        }
    }
}
