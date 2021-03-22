package denimred.simplemuseum.client.datagen;

import net.minecraft.data.DataGenerator;

import java.util.UUID;

public class MuseumTextureProvider extends TextureProvider {
    public MuseumTextureProvider(DataGenerator generator, String modId) {
        super(generator, modId);
    }

    @Override
    protected void addTextures() {
        // Uses my (DenimRed's) skin for the puppet texture
        this.addSkin(
                UUID.fromString("2f6fe476-323e-4ede-945e-927a34d38fe9"),
                "entity/museum_puppet.png");
    }
}
