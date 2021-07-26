package denimred.simplemuseum.common.entity.puppet;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetAnimationManager;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetSourceManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;

public final class PuppetDataHistorian {
    public static final byte MIN_VERSION = 0;
    public static final byte PUPPET_VERSION = 1;
    public static final String VERSION_NBT = "DataVersion";

    public static void writeVersion(CompoundNBT root) {
        root.putByte(VERSION_NBT, PUPPET_VERSION);
    }

    @SuppressWarnings({"UnnecessaryReturnStatement", "ConstantConditions"})
    public static void checkAndUpdate(CompoundNBT root) {
        final byte version = root.getByte(VERSION_NBT);
        // Short circuit for latest version
        if (version == PUPPET_VERSION) {
            return;
        }
        // Short circuit for empty tag; no need to update when there's nothing there
        if (root.isEmpty()) {
            return;
        }
        if (version < MIN_VERSION) {
            SimpleMuseum.LOGGER.warn(
                    "Illegal puppet version! (Puppet version is {}; lowest possible version is {})",
                    version,
                    MIN_VERSION);
        } else if (version < PUPPET_VERSION) {
            if (version == MIN_VERSION && updateLegacy(root)) {
                // Short circuit if all remapped tags were legacy tags
                return;
            }
            // TODO: Put more update stuff here in the future
        } else {
            SimpleMuseum.LOGGER.warn(
                    "Puppet is from the future! (Puppet version is {}; newest known version is {})",
                    version,
                    PUPPET_VERSION);
        }
    }

    /** @return True if all tags in the root were legacy tags, to short circuit later. */
    private static boolean updateLegacy(CompoundNBT root) {
        final int count = root.size();
        int changes = 0;
        if (remapLegacySchema(root, "Model", PuppetSourceManager.MODEL)) {
            changes++;
        }
        if (remapLegacySchema(root, "Texture", PuppetSourceManager.TEXTURE)) {
            changes++;
        }
        if (remapLegacySchema(root, "Animations", PuppetSourceManager.ANIMATIONS)) {
            changes++;
        }
        if (remapLegacySchema(root, "SelectedAnimation", PuppetAnimationManager.IDLE)) {
            changes++;
        }
        return changes == count;
    }

    /** @return True if the legacy tag existed and was remapped. */
    private static boolean remapLegacySchema(
            CompoundNBT root, String oldKey, PuppetValueProvider<?, ?> provider) {
        final INBT value = root.get(oldKey);
        if (value != null) {
            final CompoundNBT managerTag = root.getCompound(provider.key.manager);
            managerTag.put(provider.key.provider, value);
            root.put(provider.key.manager, managerTag);
            root.remove(oldKey);
            return true;
        }
        return false;
    }
}
