package denimred.simplemuseum.common.entity.puppet.manager.value.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.syncher.EntityDataAccessor;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

public final class CustomNameValue extends PuppetValue<Component, CustomNameProvider> {
    CustomNameValue(CustomNameProvider provider, PuppetValueManager manager) {
        super(provider, manager);
    }

    private static boolean isBlank(Component value) {
        final String text = value.getContents();
        final int length = text.length();
        if (length != 0) {
            for (int i = 0; i < length; i++) {
                if (!Character.isWhitespace(text.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Component get() {
        final Component customName = manager.puppet.getCustomName();
        return customName != null ? customName : TextComponent.EMPTY;
    }

    @Override
    public void set(Component value) {
        final boolean blank = isBlank(value);
        manager.puppet.setCustomName(blank ? null : value);
    }

    @Override
    public boolean onDataChanged(EntityDataAccessor<?> key) {
        // no-op, handled by vanilla
        return false;
    }

    @Override
    public void read(CompoundTag tag) {
        // no-op, handled by vanilla
    }

    @Override
    public void write(CompoundTag tag) {
        // no-op, handled by vanilla
    }
}
