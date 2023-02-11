package denimred.simplemuseum.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import org.lwjgl.opengl.GL11;

import denimred.simplemuseum.SimpleMuseum;

// Utility class, only extends RenderType to provide protected access
public final class MuseumRenderType extends RenderType {
    @SuppressWarnings("ConstantConditions")
    private MuseumRenderType() {
        super(null, null, VertexFormat.Mode.LINES, 0, false, false, null, null);
        throw new UnsupportedOperationException("Utility class instantiation");
    }

    public static RenderType getErrorBanners(final ResourceLocation texture) {
        final RenderType.CompositeState state =
                RenderType.CompositeState.builder()
                        .setAlphaState(DEFAULT_ALPHA)
                        .setTextureState(
                                new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .createCompositeState(false);
        return RenderType.create(
                SimpleMuseum.MOD_ID + ":error_banners",
                DefaultVertexFormat.POSITION_COLOR_TEX,
                GL11.GL_QUAD_STRIP,
                256,
                false,
                false,
                state);
    }
}
