package denimred.simplemuseum.client.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jhlabs.image.HSBAdjustFilter;
import com.jhlabs.image.ThresholdFilter;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.imageio.ImageIO;

import denimred.simplemuseum.SimpleMuseum;

public class MuseumTextureProvider extends TextureProvider {
    protected final Set<ThresholdRainbow> thresholdRainbows = new HashSet<>();

    public MuseumTextureProvider(DataGenerator generator, String modId) {
        super(generator, modId);
    }

    @Override
    protected void addTextures() {
        // Uses the mod author's skin for the puppet texture
        this.addSkin(SimpleMuseum.AUTHOR_UUID, "entity/museum_puppet");
        this.addThresholdRainbow("entity/museum_puppet", "entity/museum_puppet_rainbow", 200, 16);
    }

    @SuppressWarnings("SameParameterValue")
    protected void addThresholdRainbow(
            String source, String output, int threshold, int frameCount) {
        thresholdRainbows.add(new ThresholdRainbow(source, output, threshold, frameCount));
    }

    @Override
    public void act(DirectoryCache cache) throws IOException {
        super.act(cache);
        for (ThresholdRainbow rainbow : thresholdRainbows) {
            rainbow.render(this, cache);
        }
    }

    protected static class ThresholdRainbow {
        public final String source;
        public final String output;
        public final int threshold;
        public final int frameCount;

        public ThresholdRainbow(String source, String output, int threshold, int frameCount) {
            this.source = source;
            this.output = output;
            this.threshold = threshold;
            this.frameCount = frameCount;
        }

        public void render(TextureProvider provider, DirectoryCache cache) throws IOException {
            final Path input = provider.getPath(source, "png");
            final BufferedImage src = ImageIO.read(input.toFile());
            final int width = src.getWidth();
            final int height = src.getHeight();
            // Create an alpha mask and create the overlay to draw on the hue-shifted frames
            final ThresholdFilter thresholdFilter = new ThresholdFilter(threshold);
            final BufferedImage mask = thresholdFilter.filter(src, null);
            int[] srcPixels = thresholdFilter.getRGB(src, 0, 0, width, height, null);
            int[] maskPixels = thresholdFilter.getRGB(mask, 0, 0, width, height, null);
            for (int i = 0; i < srcPixels.length; i++) {
                int color = srcPixels[i] & 0x00ffffff; // Mask preexisting alpha
                int alpha = maskPixels[i] << 24; // Shift blue to alpha
                srcPixels[i] = color | alpha;
            }
            final BufferedImage overlay = thresholdFilter.createCompatibleDestImage(src, null);
            thresholdFilter.setRGB(overlay, 0, 0, width, height, srcPixels);
            // Create the frames, altering their hue to create the rainbow
            final float hueStep = 1.0F / frameCount;
            final BufferedImage[] frames = new BufferedImage[frameCount];
            frames[0] = src;
            final HSBAdjustFilter hsbAdjustFilter = new HSBAdjustFilter();
            for (int i = 1; i < frameCount; i++) {
                hsbAdjustFilter.setHFactor(hueStep * i);
                final BufferedImage frame = hsbAdjustFilter.filter(src, null);
                final Graphics2D g2d = frame.createGraphics();
                g2d.drawImage(overlay, 0, 0, null);
                frames[i] = frame;
                g2d.dispose();
            }
            // Draw the final image
            final BufferedImage out = new BufferedImage(width, height * frameCount, src.getType());
            final Graphics2D g2d = out.createGraphics();
            for (int i = 0; i < frameCount; i++) {
                g2d.drawImage(frames[i], 0, height * i, null);
            }
            g2d.dispose();
            // Create the animation metadata
            final JsonObject meta = new JsonObject();
            final JsonObject anim = new JsonObject();
            anim.addProperty("frametime", 0);
            anim.addProperty("interpolate", false);
            final JsonArray animFrames = new JsonArray();
            for (int i = 0; i < frameCount; i++) {
                animFrames.add(i);
            }
            anim.add("frames", animFrames);
            meta.add("animation", anim);
            // Finally, write everything
            provider.writeImage(cache, output, out, meta);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ThresholdRainbow that = (ThresholdRainbow) o;
            return threshold == that.threshold
                    && frameCount == that.frameCount
                    && source.equals(that.source)
                    && output.equals(that.output);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, output, threshold, frameCount);
        }
    }
}
