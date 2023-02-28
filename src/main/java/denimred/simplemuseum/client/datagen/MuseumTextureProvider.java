package denimred.simplemuseum.client.datagen;

import denimred.simplemuseum.SimpleMuseum;
import net.minecraft.data.DataGenerator;

public class MuseumTextureProvider extends TextureProvider {

    public MuseumTextureProvider(DataGenerator generator, String modId) {
        super(generator, modId);
    }

    @Override
    protected void addTextures() {
        // Uses the mod author's skin for the puppet texture
        this.addSkin(SimpleMuseum.AUTHOR_UUID, "entity/museum_puppet");
    }
}
