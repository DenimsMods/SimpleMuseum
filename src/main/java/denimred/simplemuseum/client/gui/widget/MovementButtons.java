package denimred.simplemuseum.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Function;

import javax.annotation.Nonnull;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.i18n.lang.GuiLang;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.messages.c2s.C2SMovePuppet;

public class MovementButtons extends Widget implements ITickable {
    public static final int ROTATE_COUNTER_CLOCKWISE = 0;
    public static final int MOVE_AWAY = 1;
    public static final int ROTATE_CLOCKWISE = 2;
    public static final int MOVE_LEFT = 3;
    public static final int CENTER = 4;
    public static final int MOVE_RIGHT = 5;
    public static final int MOVE_UP = 6;
    public static final int MOVE_TOWARDS = 7;
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
        switch (index) {
            case ROTATE_COUNTER_CLOCKWISE:
                return GuiLang.MOVE_ROTATE_CCW.asText();
            case MOVE_AWAY:
                return GuiLang.MOVE_AWAY.asText();
            case ROTATE_CLOCKWISE:
                return GuiLang.MOVE_ROTATE_CW.asText();
            case MOVE_LEFT:
                return GuiLang.MOVE_LEFT.asText();
            case CENTER:
                return GuiLang.MOVE_CENTER.asText();
            case MOVE_RIGHT:
                return GuiLang.MOVE_RIGHT.asText();
            case MOVE_UP:
                return GuiLang.MOVE_UP.asText();
            case MOVE_TOWARDS:
                return GuiLang.MOVE_TOWARDS.asText();
            case MOVE_DOWN:
                return GuiLang.MOVE_DOWN.asText();
        }
        return new StringTextComponent("Unknown button index: " + index);
    }

    public static void movePuppet(PuppetEntity puppet, int index) {
        final Minecraft mc = Minecraft.getInstance();
        final Entity viewer = mc.renderViewEntity;
        final Direction dir =
                viewer != null ? viewer.getAdjustedHorizontalFacing() : Direction.NORTH;
        Vector3d pos = puppet.getPositionVec();
        float speedMult = Screen.hasShiftDown() ? 0.25F : (Screen.hasControlDown() ? 1.0F : 0.5F);
        float yaw = puppet.rotationYaw;
        final float angle = 15.0F * (speedMult * 2.0F);
        switch (index) {
            case ROTATE_COUNTER_CLOCKWISE:
                yaw -= angle;
                break;
            case MOVE_AWAY:
                final Vector3i forward = dir.getDirectionVec();
                pos = pos.add(Vector3d.copy(forward).scale(speedMult));
                break;
            case ROTATE_CLOCKWISE:
                yaw += angle;
                break;
            case MOVE_LEFT:
                final Vector3i left = dir.rotateYCCW().getDirectionVec();
                pos = pos.add(Vector3d.copy(left).scale(speedMult));
                break;
            case CENTER:
                pos = new Vector3d(Math.floor(pos.x) + 0.5D, pos.y, Math.floor(pos.z) + 0.5D);
                break;
            case MOVE_RIGHT:
                final Vector3i right = dir.rotateY().getDirectionVec();
                pos = pos.add(Vector3d.copy(right).scale(speedMult));
                break;
            case MOVE_UP:
                pos = pos.add(new Vector3d(0, 1, 0).scale(speedMult));
                break;
            case MOVE_TOWARDS:
                final Vector3i back = dir.getOpposite().getDirectionVec();
                pos = pos.add(Vector3d.copy(back).scale(speedMult));
                break;
            case MOVE_DOWN:
                pos = pos.add(new Vector3d(0, -1, 0).scale(speedMult));
                break;
        }
        MuseumNetworking.CHANNEL.sendToServer(
                new C2SMovePuppet(puppet.getUniqueID(), pos, puppet.rotationPitch, yaw));
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
    public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
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
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        boolean b = false;
        for (MoveButton button : buttons) {
            if (button.mouseReleased(mouseX, mouseY, mouseButton)) {
                b = true;
            }
        }
        return b || super.mouseReleased(mouseX, mouseY, mouseButton);
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

    @Override
    public void tick() {
        for (final MoveButton button : buttons) {
            button.tick();
        }
    }

    @FunctionalInterface
    public interface MultiPressable {
        void press(int index);
    }

    private static final class MoveButton extends Button implements ITickable {
        private final int col;
        private final int row;
        private boolean down;

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

        @Override
        public void onClick(double mouseX, double mouseY) {
            down = true;
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            down = false;
        }

        @Override
        public void tick() {
            if (down) {
                this.onPress();
            }
        }

        @SuppressWarnings("deprecation") // >:I Mojang
        @Override
        public void renderWidget(
                MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.getTextureManager().bindTexture(MOVEMENT_BUTTONS_TEXTURE);
            int yTex = SIZE * row;
            if (!active) {
                yTex += SIZE * (DIM * 3);
            } else if (down) {
                yTex += SIZE * (DIM * 2);
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
