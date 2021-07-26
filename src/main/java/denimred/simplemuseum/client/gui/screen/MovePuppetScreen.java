package denimred.simplemuseum.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent;
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
    protected void init() {
        state.save();

        final Button backButton =
                this.addButton(
                        new Button(
                                MARGIN,
                                MARGIN,
                                WIDTH,
                                20,
                                GuiLang.PUPPET_CONFIG.asText(),
                                button -> mc.displayGuiScreen(parent)));
        movementButtons =
                this.addListener(
                        new MovementButtons(
                                MARGIN + 20,
                                backButton.y + backButton.getHeight() + MARGIN,
                                new StringTextComponent("todo"),
                                MovementButtons::getName,
                                i -> MovementButtons.movePuppet(puppet, i),
                                this::renderWidgetTooltip));
        final ITextComponent xMsg = GuiLang.PUPPET_MOVE_X.asText();
        final int xMsgWidth = font.getStringPropertyWidth(xMsg);
        xField =
                this.addListener(
                        new BoundTextFieldWidget(
                                font,
                                (MARGIN * 2) + xMsgWidth,
                                movementButtons.y + movementButtons.getHeight() + MARGIN,
                                WIDTH - xMsgWidth - MARGIN,
                                20,
                                xMsg,
                                () -> NumberUtil.parseString(puppet.getPosX())));
        final ITextComponent yMsg = GuiLang.PUPPET_MOVE_Y.asText();
        final int yMsgWidth = font.getStringPropertyWidth(yMsg);
        yField =
                this.addListener(
                        new BoundTextFieldWidget(
                                font,
                                (MARGIN * 2) + yMsgWidth,
                                xField.y + xField.getHeight() + MARGIN,
                                WIDTH - yMsgWidth - MARGIN,
                                20,
                                yMsg,
                                () -> NumberUtil.parseString(puppet.getPosY())));
        final ITextComponent zMsg = GuiLang.PUPPET_MOVE_Z.asText();
        final int zMsgWidth = font.getStringPropertyWidth(zMsg);
        zField =
                this.addListener(
                        new BoundTextFieldWidget(
                                font,
                                (MARGIN * 2) + zMsgWidth,
                                yField.y + yField.getHeight() + MARGIN,
                                WIDTH - zMsgWidth - MARGIN,
                                20,
                                zMsg,
                                () -> NumberUtil.parseString(puppet.getPosZ())));
        final ITextComponent pitchMsg = GuiLang.PUPPET_MOVE_PITCH.asText();
        final int pitchMsgWidth = font.getStringPropertyWidth(pitchMsg);
        pitchField =
                this.addListener(
                        new BoundTextFieldWidget(
                                font,
                                (MARGIN * 2) + pitchMsgWidth,
                                zField.y + zField.getHeight() + MARGIN,
                                WIDTH - pitchMsgWidth - MARGIN,
                                20,
                                pitchMsg,
                                () -> NumberUtil.parseString(puppet.rotationPitch)));
        final ITextComponent yawMsg = GuiLang.PUPPET_MOVE_YAW.asText();
        final int yawMsgWidth = font.getStringPropertyWidth(yawMsg);
        yawField =
                this.addListener(
                        new BoundTextFieldWidget(
                                font,
                                (MARGIN * 2) + yawMsgWidth,
                                pitchField.y + pitchField.getHeight() + MARGIN,
                                WIDTH - yawMsgWidth - MARGIN,
                                20,
                                yawMsg,
                                () -> NumberUtil.parseString(puppet.rotationYaw)));
        final int buttonsY = yawField.y + yawField.getHeight() + MARGIN;
        applyButton =
                this.addButton(
                        new Button(
                                MARGIN,
                                buttonsY,
                                (WIDTH / 2) - (MARGIN / 2),
                                20,
                                GuiLang.PUPPET_MOVE_APPLY.asText(),
                                button -> {
                                    final Vector3d pos = puppet.getPositionVec();
                                    final double x =
                                            NumberUtil.parseDouble(xField.getText()).orElse(pos.x);
                                    final double y =
                                            NumberUtil.parseDouble(yField.getText()).orElse(pos.y);
                                    final double z =
                                            NumberUtil.parseDouble(zField.getText()).orElse(pos.z);
                                    final float pitch =
                                            NumberUtil.parseFloat(pitchField.getText())
                                                    .orElse(puppet.rotationPitch);
                                    final float yaw =
                                            NumberUtil.parseFloat(yawField.getText())
                                                    .orElse(puppet.rotationYaw);
                                    MuseumNetworking.CHANNEL.sendToServer(
                                            new C2SMovePuppet(
                                                    puppet.getUniqueID(),
                                                    new Vector3d(x, y, z),
                                                    pitch,
                                                    yaw));
                                    xField.reset();
                                    yField.reset();
                                    zField.reset();
                                    pitchField.reset();
                                    yawField.reset();
                                }));
        resetButton =
                this.addButton(
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

        xField.setValidator(NumberUtil::isValidDouble);
        yField.setValidator(NumberUtil::isValidDouble);
        zField.setValidator(NumberUtil::isValidDouble);
        pitchField.setValidator(NumberUtil::isValidFloat);
        yawField.setValidator(NumberUtil::isValidFloat);
        applyButton.active = false;
        resetButton.active = false;

        state.load();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(matrixStack);

        drawCenteredString(
                matrixStack,
                font,
                GuiLang.PUPPET_MOVE_TITLE.asText(title),
                width / 2,
                MARGIN * 2,
                0xFFFFFF);
        movementButtons.render(matrixStack, mouseX, mouseY, partialTicks);
        xField.render(matrixStack, mouseX, mouseY, partialTicks);
        drawStringLeft(matrixStack, font, xField, xField.getMessage(), xField.isPaused());
        yField.render(matrixStack, mouseX, mouseY, partialTicks);
        drawStringLeft(matrixStack, font, yField, yField.getMessage(), yField.isPaused());
        zField.render(matrixStack, mouseX, mouseY, partialTicks);
        drawStringLeft(matrixStack, font, zField, zField.getMessage(), zField.isPaused());
        pitchField.render(matrixStack, mouseX, mouseY, partialTicks);
        drawStringLeft(
                matrixStack, font, pitchField, pitchField.getMessage(), pitchField.isPaused());
        yawField.render(matrixStack, mouseX, mouseY, partialTicks);
        drawStringLeft(matrixStack, font, yawField, yawField.getMessage(), yawField.isPaused());

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int vOffset) {
        if (mc.world != null) {
            fill(matrixStack, 0, 0, WIDTH + (MARGIN * 2), this.height, 0x90101010);
            MinecraftForge.EVENT_BUS.post(new BackgroundDrawnEvent(this, matrixStack));
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
            if (xField != null) xState = xField.getText();
            if (yField != null) yState = yField.getText();
            if (zField != null) zState = zField.getText();
            if (pitchField != null) pitchState = pitchField.getText();
            if (yawField != null) yawState = yawField.getText();
        }

        protected void load() {
            if (xState != null) xField.setText(xState);
            if (yState != null) yField.setText(yState);
            if (zState != null) zField.setText(zState);
            if (pitchState != null) pitchField.setText(pitchState);
            if (yawState != null) yawField.setText(yawState);
        }
    }
}
