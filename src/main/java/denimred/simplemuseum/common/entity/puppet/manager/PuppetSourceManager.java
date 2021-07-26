package denimred.simplemuseum.common.entity.puppet.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.checked.CheckedProvider;
import denimred.simplemuseum.common.entity.puppet.manager.value.checked.CheckedValue;
import denimred.simplemuseum.common.i18n.I18nUtil;
import software.bernie.geckolib3.resource.GeckoLibCache;

public final class PuppetSourceManager extends PuppetValueManager {
    public static final String NBT_KEY = "SourceManager";
    public static final String TRANSLATION_KEY = I18nUtil.valueManager(NBT_KEY);

    public static final CheckedProvider<ResourceLocation> MODEL =
            new CheckedProvider<>(
                    key("Model"),
                    new ResourceLocation(SimpleMuseum.MOD_ID, "geo/entity/museum_puppet.geo.json"),
                    PuppetSourceManager::validateModel);
    public static final CheckedProvider<ResourceLocation> TEXTURE =
            new CheckedProvider<>(
                    key("Texture"),
                    new ResourceLocation(SimpleMuseum.MOD_ID, "textures/entity/museum_puppet.png"),
                    PuppetSourceManager::validateTexture);
    public static final CheckedProvider<ResourceLocation> ANIMATIONS =
            new CheckedProvider<>(
                    key("Animations"),
                    new ResourceLocation(
                            SimpleMuseum.MOD_ID, "animations/entity/museum_puppet.animation.json"),
                    (puppet, o) -> {
                        puppet.animationManager.invalidateCaches();
                        puppet.animationManager.controller.markNeedsReload();
                    },
                    PuppetSourceManager::validateAnimations);

    public final CheckedValue<ResourceLocation> model = this.value(MODEL);
    public final CheckedValue<ResourceLocation> texture = this.value(TEXTURE);
    public final CheckedValue<ResourceLocation> animations = this.value(ANIMATIONS);

    public PuppetSourceManager(PuppetEntity puppet) {
        super(puppet, NBT_KEY, TRANSLATION_KEY);
    }

    private static PuppetKey key(String provider) {
        return new PuppetKey(NBT_KEY, provider);
    }

    private static boolean validateModel(PuppetEntity puppet, ResourceLocation val) {
        return !puppet.world.isRemote
                || GeckoLibCache.getInstance().getGeoModels().get(val) != null;
    }

    private static boolean validateTexture(PuppetEntity puppet, ResourceLocation val) {
        return !puppet.world.isRemote
                || Minecraft.getInstance().getResourceManager().hasResource(val);
    }

    private static boolean validateAnimations(PuppetEntity puppet, ResourceLocation val) {
        return !puppet.world.isRemote
                || GeckoLibCache.getInstance().getAnimations().get(val) != null;
    }
}
