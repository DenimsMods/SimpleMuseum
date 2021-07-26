package denimred.simplemuseum.common.i18n;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;

public class I18nUtil {
    private static final Converter<String, String> PASCAL_SNAKE_CONVERTER =
            CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);

    public static String simple(String category, String key) {
        return String.format("%s.%s.%s", category, SimpleMuseum.MOD_ID, key);
    }

    public static String valueManager(String key) {
        return simple("puppet", pascalToSnake(key));
    }

    public static String valueProvider(PuppetValueProvider<?, ?> provider) {
        return String.format(
                "%s.%s", valueManager(provider.key.manager), pascalToSnake(provider.key.provider));
    }

    public static String desc(String parent) {
        return parent + ".desc";
    }

    @SuppressWarnings("ConstantConditions")
    public static String pascalToSnake(String string) {
        return PASCAL_SNAKE_CONVERTER.convert(string);
    }
}
