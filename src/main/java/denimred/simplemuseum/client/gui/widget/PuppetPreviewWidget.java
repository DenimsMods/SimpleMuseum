package denimred.simplemuseum.client.gui.widget;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.EmptyModelData;

import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.gui.screen.PuppetConfigScreen;
import denimred.simplemuseum.client.util.PlayerRotationHelper;
import denimred.simplemuseum.client.util.ScissorUtil;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.i18n.lang.GuiLang;

public class PuppetPreviewWidget extends AbstractWidget {
    protected static final int BORDER_WIDTH = 2;
    protected static final int BUTTON_SIZE = 20;
    protected static final ResourceLocation BUTTONS_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/preview_buttons.png");
    protected static final int BUTTONS_TEXTURE_HEIGHT = 64;
    protected static final int BUTTONS_TEXTURE_WIDTH = 128;
    protected static final Minecraft MC = Minecraft.getInstance();
    protected static boolean renderBoundingBoxes;
    protected static boolean renderFloor = true;
    protected static boolean showPlayer = false;
    protected static boolean doTurntable = false;
    protected final PuppetConfigScreen parent;
    protected final List<IconButton> buttons = new ArrayList<>();
    protected final IconButton resetButton;
    protected final IconButton fullscreenButton;
    protected final IconButton[] bottomButtons;
    protected final PuppetEntity previewPuppet;
    protected final LabelWidget title;
    protected final int originalX;
    protected final int originalY;
    protected final int originalWidth;
    protected final int originalHeight;
    protected float previewScale;
    protected float previewYaw;
    protected float previewPitch;
    protected float previewX;
    protected float previewY;
    protected float fullscreenness;
    protected boolean fullscreen;
    protected boolean transitioning;

    public PuppetPreviewWidget(
            PuppetConfigScreen parent,
            int x,
            int y,
            int width,
            int height,
            PuppetEntity previewPuppet,
            Button.OnTooltip tooltip) {
        super(x, y, width, height, previewPuppet.getDisplayName());
        this.originalX = x;
        this.originalY = y;
        this.originalWidth = width;
        this.originalHeight = height;
        this.parent = parent;
        this.previewPuppet = previewPuppet;
        resetButton =
                this.makeButton(
                        0,
                        b -> this.resetPreview(),
                        tooltip,
                        GuiLang.PUPPET_PREVIEW_RESET.asText());
        fullscreenButton =
                this.makeButton(
                        1,
                        b -> {
                            transitioning = true;
                            fullscreen = !fullscreen;
                        },
                        tooltip,
                        GuiLang.PUPPET_PREVIEW_FULLSCREEN.asText());
        bottomButtons =
                new IconButton[] {
                    this.makeButton(
                            4,
                            b -> doTurntable = !doTurntable,
                            tooltip,
                            GuiLang.PUPPET_PREVIEW_TURNTABLE.asText()),
                    this.makeButton(
                            3,
                            b -> renderFloor = !renderFloor,
                            tooltip,
                            GuiLang.PUPPET_PREVIEW_FLOOR.asText()),
                    this.addButton(
                            new PlayerIconButton(
                                    0,
                                    0,
                                    MC.getUser().getGameProfile(),
                                    b -> showPlayer = !showPlayer,
                                    tooltip,
                                    GuiLang.PUPPET_PREVIEW_PLAYER.asText())),
                    this.makeButton(
                            2,
                            b -> renderBoundingBoxes = !renderBoundingBoxes,
                            tooltip,
                            GuiLang.PUPPET_PREVIEW_BOXES.asText())
                };
        title =
                new LabelWidget(
                        0,
                        0,
                        MC.font,
                        LabelWidget.AnchorX.CENTER,
                        LabelWidget.AnchorY.CENTER,
                        message);
        this.resetPreview();
        this.resetWidgetPositions();
    }

    protected IconButton makeButton(
            int index, Button.OnPress press, Button.OnTooltip tooltip, Component title) {
        // x and y are controlled by resetWidgetPositions
        final IconButton button =
                new IconButton(
                        0,
                        0,
                        BUTTON_SIZE,
                        BUTTON_SIZE,
                        BUTTONS_TEXTURE,
                        BUTTON_SIZE * index,
                        0,
                        BUTTONS_TEXTURE_HEIGHT,
                        BUTTONS_TEXTURE_WIDTH,
                        BUTTON_SIZE,
                        press,
                        tooltip,
                        title);
        return this.addButton(button);
    }

    protected IconButton addButton(IconButton button) {
        buttons.add(button);
        return button;
    }

    public void resetPreview() {
        previewScale = 2.0F;
        previewYaw = 135.0F;
        previewPitch = -25.0F;
        previewX = 0.0F;
        previewY = 0.0F;
    }

    public float getFullscreenness() {
        return fullscreenness;
    }

    protected void resetWidgetPositions() {
        final int top = y + BORDER_WIDTH;
        final int left = x + BORDER_WIDTH;
        final int right = x + width - BUTTON_SIZE - BORDER_WIDTH;
        final int bottom = y + height - BORDER_WIDTH - BUTTON_SIZE;

        resetButton.x = left;
        resetButton.y = top;
        title.x = x + width / 2;
        title.y = y + 12;
        fullscreenButton.x = right;
        fullscreenButton.y = top;

        final int length = bottomButtons.length;
        final int startX = x + width / 2 - length * BUTTON_SIZE / 2;
        for (int i = 0; i < length; i++) {
            final IconButton button = bottomButtons[i];
            button.x = startX + BUTTON_SIZE * i;
            button.y = bottom;
        }
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == 0 || button == 1;
    }

    @Override
    public void playDownSound(SoundManager manager) {
        // no-op
    }

    @Override
    public boolean mouseDragged(
            double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0) {
            previewYaw += (float) (dragX * 0.5F);
            previewPitch -= (float) (dragY * 0.5F);
            if (previewPitch > 90.0F) {
                previewPitch = 90.0F;
            } else if (previewPitch < -90.0F) {
                previewPitch = -90.0F;
            }
            return true;
        } else if (button == 1) {
            previewX += (float) (dragX);
            previewY += (float) (dragY);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        previewScale = Math.max((float) (delta * 0.15F) + previewScale, Float.MIN_VALUE);
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if (transitioning) {
            final float speed = 0.03F;
            if (fullscreen) {
                if (fullscreenness < 1.0F) {
                    fullscreenness += speed;
                    if (fullscreenness > 1.0F) {
                        fullscreenness = 1.0F;
                        transitioning = false;
                    }
                } else {
                    transitioning = false;
                }
            } else {
                if (fullscreenness > 0.0F) {
                    fullscreenness -= speed;
                    if (fullscreenness < 0.0F) {
                        fullscreenness = 0.0F;
                        transitioning = false;
                    }
                } else {
                    transitioning = false;
                }
            }
            x = (int) Mth.lerp(fullscreenness, originalX, 0.0F);
            y = (int) Mth.lerp(fullscreenness, originalY, 0.0F);
            width = Mth.ceil(Mth.lerp(fullscreenness, originalWidth, parent.width));
            height = Mth.ceil(Mth.lerp(fullscreenness, originalHeight, parent.height));
            this.resetWidgetPositions();
        }
        if (doTurntable) {
            previewYaw += 0.4F;
        }

        final AABB bounds = previewPuppet.renderManager.getRenderBounds();
        final Window window = MC.getWindow();
        final double guiScale = window.getGuiScale();
        fill(poseStack, x, y, x + width, y + height, 0x66FFFFFF);
        ScissorUtil.start(
                x + BORDER_WIDTH,
                y + BORDER_WIDTH,
                width - BORDER_WIDTH * 2,
                height - BORDER_WIDTH * 2);
        fillGradient(poseStack, x, y, x + width, y + height, 0x66000000, 0xCC000000);

        // We reset the projection matrix here in order to change the clip plane distances
        RenderSystem.pushMatrix();
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(
                0.0D,
                (double) window.getWidth() / guiScale,
                (double) window.getHeight() / guiScale,
                0.0D,
                10.0D,
                300000.0D);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0F, 0.0F, -2000.0F);

        RenderSystem.translatef(
                x + (width / 2.0F) + previewX, y + (height / 2.0F) + previewY, 0.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        final PoseStack entityPS = new PoseStack();
        entityPS.translate(0.0D, 0.0D, -400.0D);
        // This is me trying to make some scale to fit thing... poorly
        final int dm = Math.min(width, height);
        final double boundsW =
                bounds.minX > bounds.maxX ? bounds.minX - bounds.maxX : bounds.maxX - bounds.minX;
        final double boundsH =
                bounds.minY > bounds.maxY ? bounds.minY - bounds.maxY : bounds.maxY - bounds.minY;
        final double bm = Math.max(boundsW, boundsH);
        final float scale = (float) (dm / (bm * guiScale)) * previewScale;
        entityPS.scale(scale, scale, scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(previewPitch);
        Quaternion quaternion2 = Vector3f.YP.rotationDegrees(previewYaw);
        quaternion1.mul(quaternion2);
        quaternion.mul(quaternion1);
        entityPS.mulPose(quaternion);
        quaternion1.conj();
        MultiBufferSource.BufferSource buffers = MC.renderBuffers().bufferSource();
        final double yOff = -(bounds.minY + (bounds.maxY / 2.0D));
        // Render the floor
        if (renderFloor) {
            entityPS.pushPose();
            entityPS.translate(-0.5F, yOff - 1.0F, -0.5F);
            MC.getBlockRenderer()
                    .renderSingleBlock(
                            Blocks.GRASS_BLOCK.defaultBlockState(),
                            entityPS,
                            buffers,
                            0xF000F0,
                            OverlayTexture.NO_OVERLAY,
                            EmptyModelData.INSTANCE);
            entityPS.popPose();
        }
        // Render the preview puppet
        EntityRenderDispatcher dispatcher = MC.getEntityRenderDispatcher();
        dispatcher.overrideCameraOrientation(quaternion1);
        dispatcher.setRenderShadow(false);
        final boolean renderHitBoxes = dispatcher.shouldRenderHitBoxes();
        dispatcher.setRenderHitBoxes(renderBoundingBoxes);
        RenderSystem.runAsFancy(
                () -> {
                    dispatcher.render(
                            previewPuppet,
                            0.0D,
                            yOff,
                            0.0D,
                            0.0F,
                            partialTicks,
                            entityPS,
                            buffers,
                            0xF000F0);
                    this.renderFire(entityPS, buffers, dispatcher, yOff);
                });
        // Render the player for scale
        if (showPlayer && MC.player != null) {
            final float xOff = -Math.max(1.0F, (float) bounds.maxX + 0.5F);
            if (renderFloor) {
                entityPS.pushPose();
                entityPS.translate(xOff - 0.5F, yOff - 1.0F, -0.5F);
                MC.getBlockRenderer()
                        .renderSingleBlock(
                                Blocks.GRASS_BLOCK.defaultBlockState(),
                                entityPS,
                                buffers,
                                0xF000F0,
                                OverlayTexture.NO_OVERLAY,
                                EmptyModelData.INSTANCE);
                entityPS.popPose();
            }
            dispatcher.setRenderHitBoxes(false);
            PlayerRotationHelper.save();
            PlayerRotationHelper.clear();
            RenderSystem.runAsFancy(
                    () ->
                            dispatcher.render(
                                    MC.player,
                                    xOff,
                                    yOff,
                                    0.0D,
                                    0.0F,
                                    partialTicks,
                                    entityPS,
                                    buffers,
                                    0xF000F0));
            PlayerRotationHelper.load();
        }
        dispatcher.setRenderHitBoxes(renderHitBoxes);
        dispatcher.setRenderShadow(true);

        buffers.endBatch();
        RenderSystem.popMatrix();
        ScissorUtil.stop();

        poseStack.pushPose();
        poseStack.translate(0, 0, 1000);
        for (final IconButton button : buttons) {
            button.render(poseStack, mouseX, mouseY, partialTicks);
        }
        title.render(poseStack, mouseX, mouseY, partialTicks);
        poseStack.popPose();
    }

    protected void renderFire(
            PoseStack entityPS,
            MultiBufferSource.BufferSource buffers,
            EntityRenderDispatcher manager,
            double yOff) {
        // The preview copy will never be on fire otherwise, so doing this directly is fine
        if (previewPuppet.renderManager.flaming.get()) {
            entityPS.pushPose();
            entityPS.translate(0.0D, yOff, 0.0D);
            entityPS.mulPose(Vector3f.YP.rotationDegrees(manager.camera.getYRot() - previewYaw));
            manager.renderFlame(entityPS, buffers, previewPuppet);
            entityPS.popPose();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (IconButton button : buttons) {
            if (button.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (IconButton button : buttons) {
            if (button.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        for (IconButton button : buttons) {
            if (button.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        boolean b = false;
        for (IconButton button : buttons) {
            if (button.mouseReleased(mouseX, mouseY, mouseButton)) {
                b = true;
            }
        }
        return b || super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean changeFocus(boolean focus) {
        for (IconButton button : buttons) {
            if (button.changeFocus(focus)) {
                return true;
            }
        }
        return super.changeFocus(focus);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for (IconButton button : buttons) {
            if (button.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        // no-op
    }
}
