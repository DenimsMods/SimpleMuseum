package denimred.simplemuseum.common.entity.puppet.manager;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import denimred.simplemuseum.client.util.ResourceUtil;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import denimred.simplemuseum.common.entity.puppet.manager.value.checked.CheckedValue;
import denimred.simplemuseum.common.i18n.Descriptive;
import denimred.simplemuseum.common.i18n.I18nUtil;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;

import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;

public abstract class PuppetValueManager implements Descriptive {
    public final PuppetEntity puppet;
    public final String nbtKey;
    public final String i18nKey;
    public final EntityDataManager data;
    protected final Map<String, PuppetValue<?, ?>> values =
            new Object2ReferenceLinkedOpenHashMap<>();

    protected PuppetValueManager(PuppetEntity puppet, String nbtKey, String i18nKey) {
        this.puppet = puppet;
        this.nbtKey = nbtKey;
        this.i18nKey = i18nKey;
        this.data = puppet.getDataManager();
    }

    public Optional<PuppetValue<?, ?>> getValue(PuppetValueProvider<?, ?> provider) {
        return this.getValue(provider.key);
    }

    public Optional<PuppetValue<?, ?>> getValue(PuppetKey key) {
        return this.getValue(key.provider);
    }

    public Optional<PuppetValue<?, ?>> getValue(String providerKey) {
        return Optional.ofNullable(values.get(providerKey));
    }

    public List<PuppetValue<?, ?>> getValues() {
        return new ArrayList<>(values.values());
    }

    protected <T, V extends PuppetValue<T, ? extends PuppetValueProvider<T, V>>> V value(
            PuppetValueProvider<T, V> provider) {
        final V value = provider.provideFor(this);
        if (values.putIfAbsent(value.provider.key.provider, value) != null) {
            // This technically shouldn't happen since PuppetValue checks first, but still...
            throw new IllegalArgumentException("Value already defined for " + value.provider.key);
        }
        if (value.provider.dataKey != null) {
            data.register(value.provider.dataKey, value.provider.defaultValue);
        }
        return value;
    }

    /**
     * Typically called by {@link PuppetEntity#notifyDataManagerChange} to notify this manager that
     * the puppet's data manager has new values.
     *
     * @param key The key that refers to the changed data.
     * @return true if any values accepted the key.
     */
    public boolean onDataChanged(DataParameter<?> key) {
        for (PuppetValue<?, ?> value : values.values()) {
            if (value.onDataChanged(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Typically called externally by {@link PuppetEntity#readAdditional} to read NBT data from the
     * puppet entity.
     *
     * @param root The tag to read NBT data from; not this manager's designated tag.
     */
    public void read(CompoundNBT root) {
        if (root.contains(nbtKey, TAG_COMPOUND)) {
            final CompoundNBT tag = root.getCompound(nbtKey);
            for (PuppetValue<?, ?> value : values.values()) {
                value.read(tag);
            }
        }
    }

    /**
     * Typically called externally by {@link PuppetEntity#writeAdditional} to write NBT data to the
     * puppet entity.
     *
     * @param root The tag to write NBT data to; not this manager's designated tag.
     */
    public void write(CompoundNBT root) {
        final CompoundNBT tag = root.getCompound(nbtKey);
        for (PuppetValue<?, ?> value : values.values()) {
            value.write(tag);
        }
        if (!tag.isEmpty()) {
            root.put(nbtKey, tag);
        }
    }

    /**
     * Typically called externally by {@link PuppetEntity#invalidateCaches} to clear the cache of
     * this manager. Primarily used by {@link ResourceUtil#onResourceReload} to wipe client-side
     * cached values when the resources reload.
     */
    public void invalidateCaches() {
        for (PuppetValue<?, ?> value : values.values()) {
            if (value instanceof CheckedValue<?>) {
                ((CheckedValue<?>) value).invalidateCache();
            }
        }
    }

    @Override
    public IFormattableTextComponent getTitle() {
        return new TranslationTextComponent(i18nKey);
    }

    @Override
    public List<IFormattableTextComponent> getDescription() {
        return Collections.singletonList(
                new TranslationTextComponent(I18nUtil.desc(i18nKey))
                        .mergeStyle(TextFormatting.GRAY));
    }
}
