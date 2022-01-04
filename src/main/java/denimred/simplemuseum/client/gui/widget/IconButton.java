package denimred.simplemuseum.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.util.ClientUtil;

public class IconButton extends BetterButton {
    public static final ResourceLocation BLANK_BUTTON_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/blank_button.png");
    protected static final TextureManager TEXTURE_MANAGER = ClientUtil.MC.getTextureManager();
    protected final ResourceLocation resourceLocation;
    protected final int xTexStart;
    protected final int yTexStart;
    protected final int yDiffText;
    protected final int textureWidth;
    protected final int textureHeight;

    public IconButton(
            int x,
            int y,
            int width,
            int height,
            ResourceLocation resourceLocation,
            int xTexStart,
            int yTexStart,
            int textureHeight,
            int textureWidth,
            int yDiffText,
            OnPress press) {
        this(
                x,
                y,
                width,
                height,
                resourceLocation,
                xTexStart,
                yTexStart,
                textureHeight,
                textureWidth,
                yDiffText,
                press,
                NO_TOOLTIP,
                TextComponent.EMPTY);
    }

    public IconButton(
            int x,
            int y,
            int width,
            int height,
            ResourceLocation resourceLocation,
            int xTexStart,
            int yTexStart,
            int textureHeight,
            int textureWidth,
            int yDiffText,
            OnPress press,
            OnTooltip tooltip,
            Component title) {
        super(x, y, width, height, title, press, tooltip);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.yDiffText = yDiffText;
        this.resourceLocation = resourceLocation;
    }

    @SuppressWarnings("deprecation") // >:I Mojang
    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        TEXTURE_MANAGER.bind(resourceLocation);
        final int yTex = yTexStart + (yDiffText * this.getYImage(this.isHovered()));
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        blit(poseStack, x, y, xTexStart, yTex, width, height, textureWidth, textureHeight);
        if (this.isHovered()) {
            this.renderToolTip(poseStack, mouseX, mouseY);
        }
        RenderSystem.disableBlend();
    }

    protected int getYImage(boolean isHovered) {
        if (!active) {
            return 2;
        } else if (isHovered) {
            return 1;
        }
        return 0;
    }
}
