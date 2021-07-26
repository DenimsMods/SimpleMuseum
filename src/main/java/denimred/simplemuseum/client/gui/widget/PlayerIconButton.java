package denimred.simplemuseum.client.gui.widget;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.Map;

import denimred.simplemuseum.client.util.ClientUtil;

public class PlayerIconButton extends IconButton {
    protected final ResourceLocation skinLocation;

    public PlayerIconButton(
            int x,
            int y,
            GameProfile profile,
            IPressable onPress,
            ITooltip tooltip,
            ITextComponent title) {
        super(x, y, 20, 20, BLANK_BUTTON_TEXTURE, 0, 0, 64, 32, 20, onPress, tooltip, title);
        final SkinManager skinManager = ClientUtil.MC.getSkinManager();
        final Map<Type, MinecraftProfileTexture> map = skinManager.loadSkinFromCache(profile);
        if (map.containsKey(Type.SKIN)) {
            skinLocation = skinManager.loadSkin(map.get(Type.SKIN), Type.SKIN);
        } else {
            skinLocation = DefaultPlayerSkin.getDefaultSkin(PlayerEntity.getUUID(profile));
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void blitFace(MatrixStack matrixStack, int x, int y, int size) {
        blit(matrixStack, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
        blit(matrixStack, x, y, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
    }

    @Override
    public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        TEXTURE_MANAGER.bindTexture(resourceLocation);
        final int yTex = yTexStart + (yDiffText * this.getYImage(this.isHovered()));
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        blit(matrixStack, x, y, xTexStart, yTex, width, height, textureWidth, textureHeight);
        TEXTURE_MANAGER.bindTexture(skinLocation);
        RenderSystem.color4f(0.3F, 0.3F, 0.3F, alpha);
        blitFace(matrixStack, x + 4, y + 4, 13);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        blitFace(matrixStack, x + 3, y + 3, 13);
        if (this.isHovered()) {
            this.renderToolTip(matrixStack, mouseX, mouseY);
        }
    }
}
