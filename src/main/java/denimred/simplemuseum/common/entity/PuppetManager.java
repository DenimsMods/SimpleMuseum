package denimred.simplemuseum.common.entity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;

import denimred.simplemuseum.client.util.ResourceUtil;
import denimred.simplemuseum.common.util.CheckedResource;

import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;

public abstract class PuppetManager {
    protected final MuseumPuppetEntity puppet;
    protected final EntityDataManager dataManager;
    protected final String name;

    protected PuppetManager(MuseumPuppetEntity puppet, String name) {
        this.puppet = puppet;
        this.dataManager = puppet.getDataManager();
        this.name = name;
        this.registerDataKeys(); // Anti-pattern but consistent with Mojang...
    }

    /**
     * A simple utility that attempts to set a {@link CheckedResource} with a new {@link
     * ResourceLocation} using a string, no-oping if a {@link ResourceLocationException} is thrown.
     *
     * @param resource The resource to set.
     * @param value The stringed resource location to use.
     */
    protected static void trySet(CheckedResource<ResourceLocation> resource, String value) {
        try {
            resource.set(new ResourceLocation(value));
        } catch (ResourceLocationException ignored) {
            // no-op
        }
    }

    /**
     * Called by the {@link PuppetManager#PuppetManager} constructor to initialize this manager's
     * data keys. It's technically anti-pattern to call an overridable method from a constructor,
     * but this is more consistent with how Mojang does it and also I'm lazy.
     */
    protected abstract void registerDataKeys();

    /**
     * Typically called by {@link MuseumPuppetEntity#notifyDataManagerChange} to notify this manager
     * that the puppet's data manager has new values.
     *
     * @param key The key that refers to the changed data.
     */
    public abstract void onDataChanged(DataParameter<?> key);

    /**
     * Typically called externally by {@link MuseumPuppetEntity#readAdditional} to read NBT data
     * from the puppet entity.
     *
     * @param root The tag to read NBT data from; not this manager's designated tag.
     */
    public final void read(CompoundNBT root) {
        this.remapNBT(root);
        if (root.contains(name, TAG_COMPOUND)) {
            this.readNBT(root.getCompound(name));
        }
    }

    /**
     * Called by {@link #read} to read NBT data.
     *
     * @param tag This manager's tag, where all of this manager's data should be read from.
     */
    protected abstract void readNBT(CompoundNBT tag);

    /**
     * Typically called externally by {@link MuseumPuppetEntity#writeAdditional} to write NBT data
     * to the puppet entity.
     *
     * @param root The tag to write NBT data to; not this manager's designated tag.
     */
    public final void write(CompoundNBT root) {
        final CompoundNBT tag = root.getCompound(name);
        this.writeNBT(tag);
        if (!tag.isEmpty()) {
            root.put(name, tag);
        }
    }

    /**
     * Called by {@link #write} to write NBT data.
     *
     * @param tag This manager's tag, where all of this manager's data should be written.
     */
    protected abstract void writeNBT(CompoundNBT tag);

    /**
     * Called in {@link #read} before NBT data is read in order to give the manager a chance to
     * remap old data.
     *
     * @param root The tag where the manager's old data can be found. Note: This isn't the same tag
     *     that the manager uses, rather it's the one above it. Typically the mod tag.
     */
    protected void remapNBT(CompoundNBT root) {}

    /**
     * Remaps the NBT schema from previous versions to be compatible with update 1.4.0
     *
     * @param root The tag where the old key can be found, and from which the new key will be
     *     defined. Typically the mod tag.
     * @param oldKey The old tag used in versions below 1.4.0
     * @param newKey The new tag that conforms to the 1.4.0 schema
     */
    protected final void remap140(CompoundNBT root, String oldKey, String newKey) {
        final INBT value = root.get(oldKey);
        if (value != null) {
            final CompoundNBT managerTag = root.getCompound(name);
            managerTag.put(newKey, value);
            root.put(name, managerTag);
            root.remove(oldKey);
        }
    }

    /**
     * Typically called externally by {@link MuseumPuppetEntity#clearCaches} to clear the cache of
     * this manager. Primarily used by {@link ResourceUtil#onResourceReload} to wipe client-side
     * cached values when the resources reload.
     */
    public void clearCaches() {}
}
