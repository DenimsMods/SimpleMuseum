package denimred.simplemuseum.common.i18n;

import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.data.LanguageProvider;

public interface Translatable {
    String getKey();

    String getEnglish();

    default void provide(LanguageProvider provider) {
        provider.add(this.getKey(), this.getEnglish());
    }

    default TranslationTextComponent asText(Object... args) {
        return new TranslationTextComponent(this.getKey(), args);
    }
}
