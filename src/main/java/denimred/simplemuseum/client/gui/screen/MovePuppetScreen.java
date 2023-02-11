package denimred.simplemuseum.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ScreenEvent.BackgroundDrawnEvent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

import denimred.simplemuseum.client.gui.widget.BoundTextFieldWidget;
import denimred.simplemuseum.client.gui.widget.MovementButtons;
import denimred.simplemuseum.client.util.NumberUtil;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.i18n.lang.GuiLang;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.messages.c2s.C2SMovePuppet;

@Deprecated
public class MovePuppetScreen extends PuppetScreen {
    private static final int WIDTH = 100;
    private final SavedState state;
    public Button applyButton;
    public Button resetButton;
    private MovementButtons movementButtons;
    private BoundTextFieldWidget xField;
    private BoundTextFieldWidget yField;
    private BoundTextFieldWidget zField;
    private BoundTextFieldWidget pitchField;
    private BoundTextFieldWidget yawField;

    public MovePuppetScreen(PuppetEntity puppet, @Nullable Screen parent) {
        super(puppet, parent);
        state = new SavedState();
    }

    @Override
    public void init() {
        state.save();

        final Button backButton =
                this.addRenderableWidget(
                        new Button(
                                MARGIN,
                                MARGIN,
                                WIDTH,
                                20,
                                GuiLang.PUPPET_CONFIG.asText(),
                                button -> mc.setScreen(parent)));
        movementButtons =
                this.addWidget(
                        new MovementButtons(
                                MARGIN + 20,
                                backButton.y + backButton.getHeight() + MARGIN,
                                new TextComponent("todo"),
                                MovementButtons::getName,
                                i -> MovementButtons.movePuppet(puppet, i),
                                this::renderWidgetTooltip));
        final Component xMsg = GuiLang.PUPPET_MOVE_X.asText();
        final int xMsgWidth = font.width(xMsg);
        xField =
                this.addWidget(
                        new BoundTextFieldWidget(
                                font,
                                (MARGIN * 2) + xMsgWidth,
                                movementButtons.y + movementButtons.getHeight() + MARGIN,
                                WIDTH - xMsgWidth - MARGIN,
                                20,
                                xMsg,
                                () -> NumberUtil.parseString(puppet.getX())));
        final Component yMsg = GuiLang.PUPPET_MOVE_Y.asText();
        final int yMsgWidth = font.width(yMsg);
        yField =
                this.addWidget(
                        new BoundTextFieldWidget(
                                font,
                                (MARGIN * 2) + yMsgWidth,
                                xField.y + xField.getHeight() + MARGIN,
                                WIDTH - yMsgWidth - MARGIN,
                                20,
                                yMsg,
                                () -> NumberUtil.parseString(puppet.getY())));
        final Component zMsg = GuiLang.PUPPET_MOVE_Z.asText();
        final int zMsgWidth = font.width(zMsg);
        zField =
                this.addWidget(
                        new BoundTextFieldWidget(
                                font,
                                (MARGIN * 2) + zMsgWidth,
                                yField.y + yField.getHeight() + MARGIN,
                                WIDTH - zMsgWidth - MARGIN,
                                20,
                                zMsg,
                                () -> NumberUtil.parseString(puppet.getZ())));
        final Component pitchMsg = GuiLang.PUPPET_MOVE_PITCH.asText();
        final int pitchMsgWidth = font.width(pitchMsg);
        pitchField =
                this.addWidget(
                        new BoundTextFieldWidget(
                                font,
                                (MARGIN * 2) + pitchMsgWidth,
                                zField.y + zField.getHeight() + MARGIN,
                                WIDTH - pitchMsgWidth - MARGIN,
                                20,
                                pitchMsg,
                                () -> NumberUtil.parseString(puppet.xRot)));
        final Component yawMsg = GuiLang.PUPPET_MOVE_YAW.asText();
        final int yawMsgWidth = font.width(yawMsg);
        yawField =
                this.addWidget(
                        new BoundTextFieldWidget(
                                font,
                                (MARGIN * 2) + yawMsgWidth,
                                pitchField.y + pitchField.getHeight() + MARGIN,
                                WIDTH - yawMsgWidth - MARGIN,
                                20,
                                yawMsg,
                                () -> NumberUtil.parseString(puppet.yRot)));
        final int buttonsY = yawField.y + yawField.getHeight() + MARGIN;
        applyButton =
                this.addRenderableWidget(
                        new Button(
                                MARGIN,
                                buttonsY,
                                (WIDTH / 2) - (MARGIN / 2),
                                20,
                                GuiLang.PUPPET_MOVE_APPLY.asText(),
                                button -> {
                                    final Vec3 pos = puppet.position();
                                    final double x =
                                            NumberUtil.parseDouble(xField.getValue()).orElse(pos.x);
                                    final double y =
                                            NumberUtil.parseDouble(yField.getValue()).orElse(pos.y);
                                    final double z =
                                            NumberUtil.parseDouble(zField.getValue()).orElse(pos.z);
                                    final float pitch =
                                            NumberUtil.parseFloat(pitchField.getValue())
                                                    .orElse(puppet.xRot);
                                    final float yaw =
                                            NumberUtil.parseFloat(yawField.getValue())
                                                    .orElse(puppet.yRot);
                                    MuseumNetworking.CHANNEL.sendToServer(
                                            new C2SMovePuppet(
                                                    puppet.getUUID(),
                                                    new Vec3(x, y, z),
                                                    pitch,
                                                    yaw));
                                    xField.reset();
                                    yField.reset();
                                    zField.reset();
                                    pitchField.reset();
                                    yawField.reset();
                                }));
        resetButton =
                this.addRenderableWidget(
                        new Button(
                                applyButton.x + applyButton.getWidth() + MARGIN,
                                buttonsY,
                                applyButton.getWidth(),
                                20,
                                GuiLang.PUPPET_MOVE_RESET.asText(),
                                button -> {
                                    xField.reset();
                                    yField.reset();
                                    zField.reset();
                                    pitchField.reset();
                                    yawField.reset();
                                }));

        xField.setFilter(NumberUtil::isValidDouble);
        yField.setFilter(NumberUtil::isValidDouble);
        zField.setFilter(NumberUtil::isValidDouble);
        pitchField.setFilter(NumberUtil::isValidFloat);
        yawField.setFilter(NumberUtil::isValidFloat);
        applyButton.active = false;
        resetButton.active = false;

        state.load();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(poseStack);

        drawCenteredString(
                poseStack,
                font,
                GuiLang.PUPPET_MOVE_TITLE.asText(title),
                width / 2,
                MARGIN * 2,
                0xFFFFFF);
        movementButtons.render(poseStack, mouseX, mouseY, partialTicks);
        xField.render(poseStack, mouseX, mouseY, partialTicks);
        drawStringLeft(poseStack, font, xField, xField.getMessage(), xField.isPaused());
        yField.render(poseStack, mouseX, mouseY, partialTicks);
        drawStringLeft(poseStack, font, yField, yField.getMessage(), yField.isPaused());
        zField.render(poseStack, mouseX, mouseY, partialTicks);
        drawStringLeft(poseStack, font, zField, zField.getMessage(), zField.isPaused());
        pitchField.render(poseStack, mouseX, mouseY, partialTicks);
        drawStringLeft(poseStack, font, pitchField, pitchField.getMessage(), pitchField.isPaused());
        yawField.render(poseStack, mouseX, mouseY, partialTicks);
        drawStringLeft(poseStack, font, yawField, yawField.getMessage(), yawField.isPaused());

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(PoseStack poseStack, int vOffset) {
        if (mc.level != null) {
            fill(poseStack, 0, 0, WIDTH + (MARGIN * 2), this.height, 0x90101010);
            MinecraftForge.EVENT_BUS.post(new BackgroundDrawnEvent(this, poseStack));
        } else {
            this.renderDirtBackground(vOffset);
        }
    }

    @Override
    public void tick() {
        xField.tick();
        yField.tick();
        zField.tick();
        pitchField.tick();
        yawField.tick();
        if (xField.isPaused()
                || yField.isPaused()
                || zField.isPaused()
                || pitchField.isPaused()
                || yawField.isPaused()) {
            applyButton.active = true;
            resetButton.active = true;
        } else {
            applyButton.active = false;
            resetButton.active = false;
        }
        movementButtons.tick();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return movementButtons.mouseReleased(mouseX, mouseY, button)
                || super.mouseReleased(mouseX, mouseY, button);
    }

    protected class SavedState {
        protected String xState;
        protected String yState;
        protected String zState;
        protected String pitchState;
        protected String yawState;

        protected void save() {
            if (xField != null) xState = xField.getValue();
            if (yField != null) yState = yField.getValue();
            if (zField != null) zState = zField.getValue();
            if (pitchField != null) pitchState = pitchField.getValue();
            if (yawField != null) yawState = yawField.getValue();
        }

        protected void load() {
            if (xState != null) xField.setValue(xState);
            if (yState != null) yField.setValue(yState);
            if (zState != null) zField.setValue(zState);
            if (pitchState != null) pitchField.setValue(pitchState);
            if (yawState != null) yawField.setValue(yawState);
        }
    }
}
