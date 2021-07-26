package denimred.simplemuseum.client.renderer.texture;

import com.mojang.blaze3d.systems.IRenderCall;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public final class AnimatedEntityTexture extends Texture implements ITickable {
    private final ResourceLocation location;
    private AnimationMetadataSection meta = AnimationMetadataSection.EMPTY;
    private NativeImage image;
    private int frameWidth;
    private int frameHeight;
    private int[] framesX;
    private int[] framesY;
    private int ticks;
    private int frameCounter;

    public AnimatedEntityTexture(ResourceLocation location) {
        this.location = location;
    }

    public static void bindOrLoad(TextureManager manager, ResourceLocation location) {
        if (manager.mapTextureObjects.containsKey(location)
                || !manager.resourceManager.hasResource(
                        new ResourceLocation(
                                location.getNamespace(), location.getPath() + ".mcmeta"))) {
            manager.bindTexture(location);
        } else {
            final AnimatedEntityTexture texture = new AnimatedEntityTexture(location);
            final IRenderCall call =
                    () -> {
                        manager.loadTexture(location, texture);
                        manager.bindTexture(location);
                    };
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(call);
            } else {
                call.execute();
            }
        }
    }

    @Override
    public void loadTexture(IResourceManager manager) throws IOException {
        try (final IResource resource = manager.getResource(location)) {
            final PngSizeInfo info =
                    new PngSizeInfo(resource.toString(), resource.getInputStream());
            final AnimationMetadataSection maybeMeta =
                    resource.getMetadata(AnimationMetadataSection.SERIALIZER);
            if (maybeMeta != null) {
                meta = maybeMeta;
            }

            Pair<Integer, Integer> size = meta.getSpriteSize(info.width, info.height);
            frameWidth = size.getFirst();
            frameHeight = size.getSecond();
        }
    }

    @Override
    public void tick() {
        ++ticks;
        if (ticks >= meta.getFrameTimeSingle(frameCounter)) {
            int i = meta.getFrameIndex(frameCounter);
            int j = meta.getFrameCount() == 0 ? this.getFrameCount() : meta.getFrameCount();
            frameCounter = (frameCounter + 1) % j;
            ticks = 0;
            int k = meta.getFrameIndex(frameCounter);
            if (i != k && k >= 0 && k < this.getFrameCount()) {
                //                this.uploadFrames(k);
            }
        }
    }

    public int getFrameCount() {
        return this.framesX.length;
    }
}
