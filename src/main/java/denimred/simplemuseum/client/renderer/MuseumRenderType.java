package denimred.simplemuseum.client.renderer;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import denimred.simplemuseum.SimpleMuseum;

// Utility class, only extends RenderType to provide protected access
public final class MuseumRenderType extends RenderType {
    @SuppressWarnings("ConstantConditions")
    private MuseumRenderType() {
        super(null, null, 0, 0, false, false, null, null);
        throw new UnsupportedOperationException("Utility class instantiation");
    }

    public static RenderType getErrorBanners(final ResourceLocation texture) {
        final RenderType.State state =
                RenderType.State.getBuilder()
                        .alpha(DEFAULT_ALPHA)
                        .texture(new RenderState.TextureState(texture, false, false))
                        .transparency(ADDITIVE_TRANSPARENCY)
                        .build(false);
        return RenderType.makeType(
                SimpleMuseum.MOD_ID + ":error_banners",
                DefaultVertexFormats.POSITION_TEX,
                GL11.GL_QUAD_STRIP,
                256,
                false,
                false,
                state);
    }
}
