package denimred.simplemuseum.client.datagen;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;

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

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

public abstract class TextureProvider implements DataProvider {
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
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
    public void run(HashCache cache) throws IOException {
        this.addTextures();
        for (Map.Entry<GameProfile, String> entry : desiredSkins.entries()) {
            this.downloadSkin(entry.getKey(), entry.getValue(), cache);
        }
    }

    protected abstract void addTextures();

    @SuppressWarnings("SameParameterValue")
    protected void addSkin(UUID uuid, String location) {
        final GameProfile profile = new GameProfile(uuid, "Datagen Fake Profile");
        if (!desiredSkins.containsKey(profile)) {
            SESSION.fillProfileProperties(profile, true);
        }
        desiredSkins.put(profile, location);
    }

    protected void downloadSkin(GameProfile profile, String location, HashCache cache)
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

    protected void writeImage(HashCache cache, String location, RenderedImage image)
            throws IOException {
        this.writeImage(cache, location, image, null);
    }

    protected void writeImage(
            HashCache cache, String location, RenderedImage image, @Nullable JsonElement meta)
            throws IOException {
        // A rough copy of IDataProvider.save, but for images
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        final byte[] data = outputStream.toByteArray();
        final String hash = SHA1.hashBytes(data).toString();
        final Path pngOut = this.getPath(location, "png");
        if (!Files.exists(pngOut) || !Objects.equals(cache.getHash(pngOut), hash)) {
            Files.createDirectories(pngOut.getParent());
            Files.write(pngOut, data);
        }
        cache.putNew(pngOut, hash);
        // Write the metadata if applicable
        if (meta != null) {
            DataProvider.save(GSON, cache, meta, this.getPath(location, "png.mcmeta"));
        }
    }

    protected Path getPath(String location, String extension) {
        return generator
                .getOutputFolder()
                .resolve(String.format("assets/%s/textures/%s.%s", modId, location, extension));
    }

    @Override
    public String getName() {
        return "Textures: " + modId;
    }
}
