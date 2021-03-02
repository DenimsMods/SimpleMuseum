package denimred.simplemuseum.common.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.ResourceLocation;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.init.MuseumDataSerializers;
import denimred.simplemuseum.common.util.CheckedResource;
import software.bernie.geckolib3.resource.GeckoLibCache;

import static net.minecraftforge.common.util.Constants.NBT.TAG_STRING;

public class PuppetSourceManager extends PuppetManager {
    // The root NBT key that this manager uses
    public static final String SOURCE_MANAGER_NBT = "SourceManager";
    // NBT keys for managed variables
    public static final String MODEL_NBT = "Model";
    public static final String TEXTURE_NBT = "Texture";
    public static final String ANIMATIONS_NBT = "Animations";
    // Data keys for managed variables
    public static final DataParameter<ResourceLocation> MODEL_KEY =
            MuseumPuppetEntity.createKeyContextual(MuseumDataSerializers.resourceLocation());
    public static final DataParameter<ResourceLocation> TEXTURE_KEY =
            MuseumPuppetEntity.createKeyContextual(MuseumDataSerializers.resourceLocation());
    public static final DataParameter<ResourceLocation> ANIMATIONS_KEY =
            MuseumPuppetEntity.createKeyContextual(MuseumDataSerializers.resourceLocation());
    // Defaults for managed variables
    public static final ResourceLocation MODEL_DEFAULT =
            new ResourceLocation(SimpleMuseum.MOD_ID, "geo/entity/museum_puppet.geo.json");
    public static final ResourceLocation TEXTURE_DEFAULT =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/entity/museum_puppet.png");
    public static final ResourceLocation ANIMATIONS_DEFAULT =
            new ResourceLocation(SimpleMuseum.MOD_ID, "animations/entity/museum_puppet.json");
    // Managed variables
    public final CheckedResource<ResourceLocation> model =
            new CheckedResource<>(
                    MODEL_DEFAULT,
                    loc -> GeckoLibCache.getInstance().getGeoModels().get(loc) != null,
                    loc -> dataManager.set(MODEL_KEY, loc));
    public final CheckedResource<ResourceLocation> texture =
            new CheckedResource<>(
                    TEXTURE_DEFAULT,
                    loc -> Minecraft.getInstance().getResourceManager().getResource(loc),
                    loc -> dataManager.set(TEXTURE_KEY, loc));
    public final CheckedResource<ResourceLocation> animations =
            new CheckedResource<>(
                    ANIMATIONS_DEFAULT,
                    loc -> GeckoLibCache.getInstance().getAnimations().get(loc) != null,
                    loc -> {
                        dataManager.set(ANIMATIONS_KEY, loc);
                        puppet.animationManager.controller.markNeedsReload();
                    });

    public PuppetSourceManager(MuseumPuppetEntity puppet) {
        super(puppet, SOURCE_MANAGER_NBT);
    }

    @Override
    public void registerDataKeys() {
        dataManager.register(MODEL_KEY, MODEL_DEFAULT);
        dataManager.register(TEXTURE_KEY, TEXTURE_DEFAULT);
        dataManager.register(ANIMATIONS_KEY, ANIMATIONS_DEFAULT);
    }

    @Override
    public void onDataChanged(DataParameter<?> key) {
        if (key.equals(MODEL_KEY)) model.set(dataManager.get(MODEL_KEY));
        else if (key.equals(TEXTURE_KEY)) texture.set(dataManager.get(TEXTURE_KEY));
        else if (key.equals(ANIMATIONS_KEY)) animations.set(dataManager.get(ANIMATIONS_KEY));
    }

    @Override
    public void readNBT(CompoundNBT tag) {
        if (tag.contains(MODEL_NBT, TAG_STRING)) trySet(model, tag.getString(MODEL_NBT));
        if (tag.contains(TEXTURE_NBT, TAG_STRING)) trySet(texture, tag.getString(TEXTURE_NBT));
        if (tag.contains(ANIMATIONS_NBT, TAG_STRING))
            trySet(animations, tag.getString(ANIMATIONS_NBT));
    }

    @Override
    public void writeNBT(CompoundNBT tag) {
        final ResourceLocation model = this.model.getDirect();
        if (!model.equals(MODEL_DEFAULT)) tag.putString(MODEL_NBT, model.toString());
        final ResourceLocation texture = this.texture.getDirect();
        if (!texture.equals(TEXTURE_DEFAULT)) tag.putString(TEXTURE_NBT, texture.toString());
        final ResourceLocation animations = this.animations.getDirect();
        if (!animations.equals(ANIMATIONS_DEFAULT))
            tag.putString(ANIMATIONS_NBT, animations.toString());
    }

    @Override
    public void remapNBT(CompoundNBT root) {
        this.remap140(root, "Model", MODEL_NBT);
        this.remap140(root, "Texture", TEXTURE_NBT);
        this.remap140(root, "Animations", ANIMATIONS_NBT);
    }

    @Override
    public void clearCaches() {
        model.clearCache();
        texture.clearCache();
        animations.clearCache();
    }
}
