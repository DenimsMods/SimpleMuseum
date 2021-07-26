package denimred.simplemuseum.common.i18n;

import net.minecraft.util.text.IFormattableTextComponent;

import java.util.Collections;
import java.util.List;

public interface Descriptive {
    IFormattableTextComponent getTitle();

    List<IFormattableTextComponent> getDescription();

    default List<IFormattableTextComponent> getAdvancedDescription() {
        return Collections.emptyList();
    }

    default boolean hideDescription() {
        return true;
    }
}
