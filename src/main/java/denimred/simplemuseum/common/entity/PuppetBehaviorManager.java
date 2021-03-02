package denimred.simplemuseum.common.entity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;

public class PuppetBehaviorManager extends PuppetManager {
    // The root NBT key that this manager uses
    public static final String BEHAVIOR_MANAGER_NBT = "BehaviorManager";

    protected PuppetBehaviorManager(MuseumPuppetEntity puppet) {
        super(puppet, BEHAVIOR_MANAGER_NBT);
    }

    @Override
    protected void registerDataKeys() {}

    @Override
    public void onDataChanged(DataParameter<?> key) {}

    @Override
    protected void readNBT(CompoundNBT tag) {}

    @Override
    protected void writeNBT(CompoundNBT tag) {}
}
