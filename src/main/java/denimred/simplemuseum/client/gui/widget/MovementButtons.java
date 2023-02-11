package denimred.simplemuseum.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

import javax.annotation.Nonnull;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.i18n.lang.GuiLang;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.messages.c2s.C2SMovePuppet;

public class MovementButtons extends AbstractWidget implements Tickable {
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
            Component title,
            Function<Integer, Component> titleMapper,
            MultiPressable press,
            Button.OnTooltip tooltip) {
        super(x, y, SIZE * DIM, SIZE * DIM, title);
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = makeButton(x, y, titleMapper, press, tooltip, i);
        }
    }

    public static MutableComponent getName(int index) {
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
        return new TextComponent("Unknown button index: " + index);
    }

    public static void movePuppet(PuppetEntity puppet, int index) {
        final Minecraft mc = Minecraft.getInstance();
        final Entity viewer = mc.cameraEntity;
        final Direction dir = viewer != null ? viewer.getMotionDirection() : Direction.NORTH;
        Vec3 pos = puppet.position();
        float speedMult = Screen.hasShiftDown() ? 0.25F : (Screen.hasControlDown() ? 1.0F : 0.5F);
        float yaw = puppet.yRot;
        final float angle = 15.0F * (speedMult * 2.0F);
        switch (index) {
            case ROTATE_COUNTER_CLOCKWISE:
                yaw -= angle;
                break;
            case MOVE_AWAY:
                final Vec3i forward = dir.getNormal();
                pos = pos.add(Vec3.atLowerCornerOf(forward).scale(speedMult));
                break;
            case ROTATE_CLOCKWISE:
                yaw += angle;
                break;
            case MOVE_LEFT:
                final Vec3i left = dir.getCounterClockWise().getNormal();
                pos = pos.add(Vec3.atLowerCornerOf(left).scale(speedMult));
                break;
            case CENTER:
                pos = new Vec3(Math.floor(pos.x) + 0.5D, pos.y, Math.floor(pos.z) + 0.5D);
                break;
            case MOVE_RIGHT:
                final Vec3i right = dir.getClockWise().getNormal();
                pos = pos.add(Vec3.atLowerCornerOf(right).scale(speedMult));
                break;
            case MOVE_UP:
                pos = pos.add(new Vec3(0, 1, 0).scale(speedMult));
                break;
            case MOVE_TOWARDS:
                final Vec3i back = dir.getOpposite().getNormal();
                pos = pos.add(Vec3.atLowerCornerOf(back).scale(speedMult));
                break;
            case MOVE_DOWN:
                pos = pos.add(new Vec3(0, -1, 0).scale(speedMult));
                break;
        }
        MuseumNetworking.CHANNEL.sendToServer(
                new C2SMovePuppet(puppet.getUUID(), pos, puppet.xRot, yaw));
    }

    @Nonnull
    private static MoveButton makeButton(
            int x,
            int y,
            Function<Integer, Component> titleMapper,
            MultiPressable press,
            Button.OnTooltip tooltip,
            int i) {
        final int col = i % DIM;
        final int row = (i - col) / DIM;
        return new MoveButton(x, y, col, row, titleMapper.apply(i), b -> press.press(i), tooltip);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        for (final MoveButton button : buttons) {
            button.render(poseStack, mouseX, mouseY, partialTicks);
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

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        // no-op
    }

    @FunctionalInterface
    public interface MultiPressable {
        void press(int index);
    }

    private static final class MoveButton extends Button implements Tickable {
        private final int col;
        private final int row;
        private boolean down;

        public MoveButton(
                int x, int y, int col, int row, Component title, OnPress press, OnTooltip tooltip) {
            super(x + (SIZE * col), y + (SIZE * row), SIZE, SIZE, title, press, tooltip);
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
        public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, MOVEMENT_BUTTONS_TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            int yTex = SIZE * row;
            if (!active) {
                yTex += SIZE * (DIM * 3);
            } else if (down) {
                yTex += SIZE * (DIM * 2);
            } else if (this.isHoveredOrFocused()) {
                yTex += SIZE * DIM;
            }

            RenderSystem.enableDepthTest();
            blit(poseStack, x, y, SIZE * col, yTex, width, height, 64, 256);
            if (this.isHoveredOrFocused()) {
                this.renderToolTip(poseStack, mouseX, mouseY);
            }
        }
    }
}
