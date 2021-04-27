package denimred.simplemuseum.common.entity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

import java.util.function.Predicate;

import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.init.MuseumDataSerializers;
import denimred.simplemuseum.common.util.CheckedResource;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.SoundKeyframeEvent;

import static net.minecraftforge.common.util.Constants.NBT.TAG_STRING;

public class PuppetAudioManager extends PuppetManager {
    // The root NBT key that this manager uses
    public static final String AUDIO_MANAGER_NBT = "AudioManager";
    // NBT keys for managed variables
    public static final String AMBIENT_NBT = "Ambient";
    public static final String CATEGORY_NBT = "Category";
    // Data keys for managed variables
    public static final DataParameter<ResourceLocation> AMBIENT_KEY =
            MuseumPuppetEntity.createKeyContextual(MuseumDataSerializers.resourceLocation());
    public static final DataParameter<SoundCategory> CATEGORY_KEY =
            MuseumPuppetEntity.createKeyContextual(MuseumDataSerializers.soundCategory());
    // Defaults for managed variables
    public static final ResourceLocation AMBIENT_DEFAULT = new ResourceLocation("", "");
    public static final SoundCategory CATEGORY_DEFAULT = SoundCategory.NEUTRAL;
    // Managed variables
    public final CheckedResource<ResourceLocation> ambient =
            new CheckedResource<>(
                    AMBIENT_DEFAULT,
                    (Predicate<ResourceLocation>) PuppetAudioManager::isSoundValid,
                    loc -> dataManager.set(AMBIENT_KEY, loc));
    private SoundCategory category = CATEGORY_DEFAULT;

    protected PuppetAudioManager(MuseumPuppetEntity puppet) {
        super(puppet, AUDIO_MANAGER_NBT);
    }

    private static boolean isSoundValid(ResourceLocation soundName) {
        return ClientUtil.MC.getSoundHandler().getAvailableSounds().contains(soundName);
    }

    public void playAmbientSound() {
        if (puppet.world.isRemote) {
            // We don't use getSafe here since the default isn't technically valid either
            final ResourceLocation ambientSound = ambient.getDirect();
            if (isSoundValid(ambientSound)) {
                this.playClientSound(ambientSound);
            }
        }
    }

    public <T extends IAnimatable> void playAnimSound(SoundKeyframeEvent<T> event) {
        final ResourceLocation soundName = ResourceLocation.tryCreate(event.sound);
        if (soundName != null && isSoundValid(soundName)) {
            this.playClientSound(soundName);
        }
    }

    private void playClientSound(ResourceLocation soundName) {
        ClientUtil.playArbitrarySound(
                soundName, category, puppet.getBoundingBox().getCenter(), 1.0F, 1.0F);
    }

    public SoundCategory getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if (SoundCategory.SOUND_CATEGORIES.containsKey(category)) {
            this.setCategory(SoundCategory.SOUND_CATEGORIES.get(category));
        }
    }

    public void setCategory(SoundCategory category) {
        this.category = category;
        dataManager.set(CATEGORY_KEY, category);
    }

    @Override
    public void registerDataKeys() {
        dataManager.register(AMBIENT_KEY, AMBIENT_DEFAULT);
        dataManager.register(CATEGORY_KEY, CATEGORY_DEFAULT);
    }

    @Override
    public void onDataChanged(DataParameter<?> key) {
        if (key.equals(AMBIENT_KEY)) ambient.set(dataManager.get(AMBIENT_KEY));
        else if (key.equals(CATEGORY_KEY)) this.setCategory(dataManager.get(CATEGORY_KEY));
    }

    @Override
    protected void readNBT(CompoundNBT tag) {
        if (tag.contains(AMBIENT_NBT, TAG_STRING)) trySet(ambient, tag.getString(AMBIENT_NBT));
        if (tag.contains(CATEGORY_NBT, TAG_STRING)) this.setCategory(tag.getString(CATEGORY_NBT));
    }

    @Override
    protected void writeNBT(CompoundNBT tag) {
        final ResourceLocation amb = ambient.getDirect();
        if (!amb.equals(AMBIENT_DEFAULT)) tag.putString(AMBIENT_NBT, amb.toString());
        if (!category.equals(CATEGORY_DEFAULT)) tag.putString(CATEGORY_NBT, category.getName());
    }

    @Override
    public void clearCaches() {
        ambient.clearCache();
    }
}
