package denimred.simplemuseum.common.i18n;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.data.LanguageProvider;

public interface Translatable {
    String getKey();

    String getEnglish();

    default void provide(LanguageProvider provider) {
        provider.add(this.getKey(), this.getEnglish());
    }

    default TranslatableComponent asText(Object... args) {
        return new TranslatableComponent(this.getKey(), args);
    }
}
