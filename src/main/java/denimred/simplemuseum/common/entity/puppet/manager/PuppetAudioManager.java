package denimred.simplemuseum.common.entity.puppet.manager;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.checked.CheckedProvider;
import denimred.simplemuseum.common.entity.puppet.manager.value.checked.CheckedValue;
import denimred.simplemuseum.common.entity.puppet.manager.value.standard.BasicProvider;
import denimred.simplemuseum.common.entity.puppet.manager.value.standard.BasicValue;
import denimred.simplemuseum.common.i18n.I18nUtil;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.SoundKeyframeEvent;

public final class PuppetAudioManager extends PuppetValueManager {
    public static final String NBT_KEY = "AudioManager";
    public static final String TRANSLATION_KEY = I18nUtil.valueManager(NBT_KEY);

    public static final CheckedProvider<ResourceLocation> AMBIENT =
            new CheckedProvider<>(
                    key("Ambient"),
                    new ResourceLocation(SimpleMuseum.MOD_ID, "misc/silence"),
                    PuppetAudioManager::isSoundValid);
    public static final BasicProvider<SoundCategory> CATEGORY =
            new BasicProvider<>(key("Category"), SoundCategory.NEUTRAL);

    public final CheckedValue<ResourceLocation> ambient = this.value(AMBIENT);
    public final BasicValue<SoundCategory> category = this.value(CATEGORY);

    public PuppetAudioManager(PuppetEntity puppet) {
        super(puppet, NBT_KEY, TRANSLATION_KEY);
    }

    private static PuppetKey key(String provider) {
        return new PuppetKey(NBT_KEY, provider);
    }

    private static boolean isSoundValid(PuppetEntity puppet, ResourceLocation soundName) {
        return !puppet.world.isRemote
                || ClientUtil.MC.getSoundHandler().getAvailableSounds().contains(soundName);
    }

    public void playAmbientSound() {
        if (puppet.world.isRemote) {
            // We don't use getSafe here since the default isn't technically valid either
            final ResourceLocation ambientSound = ambient.get();
            if (isSoundValid(puppet, ambientSound)) {
                this.playClientSound(ambientSound);
            }
        }
    }

    public <T extends IAnimatable> void playAnimSound(SoundKeyframeEvent<T> event) {
        final ResourceLocation soundName = ResourceLocation.tryCreate(event.sound);
        if (soundName != null && isSoundValid(puppet, soundName)) {
            this.playClientSound(soundName);
        }
    }

    private void playClientSound(ResourceLocation soundName) {
        ClientUtil.playArbitrarySound(
                soundName, category.get(), puppet.getBoundingBox().getCenter(), 1.0F, 1.0F);
    }
}
