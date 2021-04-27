package denimred.simplemuseum.client.datagen;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.imageio.ImageIO;

@SuppressWarnings("UnstableApiUsage")
public abstract class TextureProvider implements IDataProvider {
    protected static final MinecraftSessionService SESSION =
            new YggdrasilAuthenticationService(Proxy.NO_PROXY).createMinecraftSessionService();
    protected final Multimap<GameProfile, String> desiredSkins =
            MultimapBuilder.hashKeys().hashSetValues().build();
    protected final DataGenerator generator;
    protected final String modId;

    protected TextureProvider(DataGenerator generator, String modId) {
        this.generator = generator;
        this.modId = modId;
    }

    @Override
    public void act(DirectoryCache cache) throws IOException {
        this.addTextures();
        for (Map.Entry<GameProfile, String> entry : desiredSkins.entries()) {
            this.downloadSkin(entry.getKey(), entry.getValue(), cache);
        }
    }

    protected abstract void addTextures();

    protected void addSkin(UUID uuid, String location) {
        final GameProfile profile = new GameProfile(uuid, "Datagen Fake Profile");
        if (!desiredSkins.containsKey(profile)) {
            SESSION.fillProfileProperties(profile, true);
        }
        desiredSkins.put(profile, location);
    }

    protected void downloadSkin(GameProfile profile, String location, DirectoryCache cache)
            throws IOException {
        final MinecraftProfileTexture texture =
                SESSION.getTextures(profile, true).get(MinecraftProfileTexture.Type.SKIN);
        if (texture != null) {
            final URL url = new URL(texture.getUrl());
            final BufferedImage image = ImageIO.read(url);
            this.writeImage(cache, location, image);
        } else {
            throw new IllegalArgumentException(
                    "Skin texture missing for profile: " + profile.getId());
        }
    }

    protected void writeImage(DirectoryCache cache, String location, RenderedImage image)
            throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        final byte[] data = outputStream.toByteArray();
        final String hash = HASH_FUNCTION.hashBytes(data).toString();
        final Path output =
                generator.getOutputFolder().resolve("assets/" + modId + "/textures/" + location);
        if (!Files.exists(output) || !Objects.equals(cache.getPreviousHash(output), hash)) {
            Files.createDirectories(output.getParent());
            Files.write(output, data);
        }
        cache.recordHash(output, hash);
    }

    @Override
    public String getName() {
        return "Textures: " + modId;
    }
}
