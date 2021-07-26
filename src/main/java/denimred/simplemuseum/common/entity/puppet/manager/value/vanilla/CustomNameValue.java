package denimred.simplemuseum.common.entity.puppet.manager.value.vanilla;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

public final class CustomNameValue extends PuppetValue<ITextComponent, CustomNameProvider> {
    protected CustomNameValue(CustomNameProvider provider, PuppetValueManager manager) {
        super(provider, manager);
    }

    private static boolean isBlank(ITextComponent value) {
        final String text = value.getUnformattedComponentText();
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
    public ITextComponent get() {
        final ITextComponent customName = manager.puppet.getCustomName();
        return customName != null ? customName : StringTextComponent.EMPTY;
    }

    @Override
    public void set(ITextComponent value) {
        final boolean blank = isBlank(value);
        manager.puppet.setCustomName(blank ? null : value);
    }

    @Override
    public boolean onDataChanged(DataParameter<?> key) {
        // no-op, handled by vanilla
        return false;
    }

    @Override
    public void read(CompoundNBT tag) {
        // no-op, handled by vanilla
    }

    @Override
    public void write(CompoundNBT tag) {
        // no-op, handled by vanilla
    }
}
