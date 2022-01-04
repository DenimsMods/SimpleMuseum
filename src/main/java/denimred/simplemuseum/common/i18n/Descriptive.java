package denimred.simplemuseum.common.i18n;

import net.minecraft.network.chat.MutableComponent;

import java.util.Collections;
import java.util.List;

public interface Descriptive {
    MutableComponent getTitle();

    List<MutableComponent> getDescription();

    default List<MutableComponent> getAdvancedDescription() {
        return Collections.emptyList();
    }

    default boolean hideDescription() {
        return true;
    }
}
