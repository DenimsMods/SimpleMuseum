package denimred.simplemuseum.common.item;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.EntityType;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class DeferredSpawnEgg extends SpawnEggItem {
    public static final IItemColor COLOR =
            (stack, tintIndex) -> ((DeferredSpawnEgg) stack.getItem()).getColor(tintIndex);
    private static final List<DeferredSpawnEgg> DEFERRED_EGGS = new ArrayList<>();
    private final Lazy<? extends EntityType<?>> lazy;

    public DeferredSpawnEgg(
            Supplier<? extends EntityType<?>> typeIn,
            int primaryColor,
            int secondaryColor,
            Properties properties) {
        //noinspection ConstantConditions: We defer the type registration for later
        super(null, primaryColor, secondaryColor, properties);
        EGGS.remove(null); // Remove the null that we just added
        this.lazy = Lazy.of(typeIn);
        DEFERRED_EGGS.add(this);
    }

    public static void initialize() {
        for (DeferredSpawnEgg egg : DEFERRED_EGGS) {
            EGGS.put(egg.getType(null), egg);
        }
        DEFERRED_EGGS.clear();
    }

    @Override
    public EntityType<?> getType(@Nullable CompoundNBT nbt) {
        if (nbt != null && nbt.contains("EntityTag", 10)) {
            CompoundNBT compoundnbt = nbt.getCompound("EntityTag");
            if (compoundnbt.contains("id", 8)) {
                return EntityType.byKey(compoundnbt.getString("id")).orElse(lazy.get());
            }
        }

        return lazy.get();
    }
}
