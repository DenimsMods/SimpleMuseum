package denimred.simplemuseum.client.gui.widget;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

import denimred.simplemuseum.client.util.ClientUtil;

public class PlayerIconButton extends IconButton {
    protected final ResourceLocation skinLocation;

    public PlayerIconButton(
            int x, int y, GameProfile profile, OnPress press, OnTooltip tooltip, Component title) {
        super(x, y, 20, 20, BLANK_BUTTON_TEXTURE, 0, 0, 64, 32, 20, press, tooltip, title);
        final SkinManager skinManager = ClientUtil.MC.getSkinManager();
        final Map<Type, MinecraftProfileTexture> map =
                skinManager.getInsecureSkinInformation(profile);
        if (map.containsKey(Type.SKIN)) {
            skinLocation = skinManager.registerTexture(map.get(Type.SKIN), Type.SKIN);
        } else {
            skinLocation = DefaultPlayerSkin.getDefaultSkin(Player.createPlayerUUID(profile));
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void blitFace(PoseStack poseStack, int x, int y, int size) {
        blit(poseStack, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
        blit(poseStack, x, y, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, resourceLocation);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        final int yTex = yTexStart + (yDiffText * this.getYImage(this.isHoveredOrFocused()));
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        blit(poseStack, x, y, xTexStart, yTex, width, height, textureWidth, textureHeight);
        RenderSystem.setShaderTexture(0, skinLocation);
        RenderSystem.setShaderColor(0.3F, 0.3F, 0.3F, alpha);
        blitFace(poseStack, x + 4, y + 4, 13);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        blitFace(poseStack, x + 3, y + 3, 13);
        if (this.isHoveredOrFocused()) {
            this.renderToolTip(poseStack, mouseX, mouseY);
        }
    }
}
