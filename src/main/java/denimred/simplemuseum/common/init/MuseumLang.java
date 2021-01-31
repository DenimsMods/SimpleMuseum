package denimred.simplemuseum.common.init;

import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.fml.DatagenModLoader;

import java.util.ArrayList;
import java.util.List;

import static denimred.simplemuseum.SimpleMuseum.MOD_ID;

public final class MuseumLang {
    private static final List<PreMappedLang> LANGS_DATA = new ArrayList<>();
    // Categories
    private static final String GUI = "gui";
    // Langs
    public static final PreMappedLang GUI_SEARCH = new PreMappedLang(GUI, "search", "Search");
    public static final PreMappedLang GUI_LOADING = new PreMappedLang(GUI, "loading", "Loading...");
    public static final PreMappedLang GUI_ERROR =
            new PreMappedLang(GUI, "error", "Error, check log!");
    public static final PreMappedLang GUI_CLIPBOARD_COPY =
            new PreMappedLang(GUI, "clipboard.copy", "Copy to Clipboard");
    public static final PreMappedLang GUI_CLIPBOARD_PASTE =
            new PreMappedLang(GUI, "clipboard.paste", "Paste from Clipboard");
    public static final PreMappedLang GUI_DUMMY_MODEL =
            new PreMappedLang(GUI, "dummy.model", "Model Resource");
    public static final PreMappedLang GUI_DUMMY_MODEL_SELECT =
            new PreMappedLang(GUI, "dummy.model.select", "Select Model Resource");
    public static final PreMappedLang GUI_DUMMY_TEXTURE =
            new PreMappedLang(GUI, "dummy.texture", "Texture Resource");
    public static final PreMappedLang GUI_DUMMY_TEXTURE_SELECT =
            new PreMappedLang(GUI, "dummy.texture.select", "Select Texture Resource");
    public static final PreMappedLang GUI_DUMMY_ANIMATIONS =
            new PreMappedLang(GUI, "dummy.animations", "Animations Resource");
    public static final PreMappedLang GUI_DUMMY_ANIMATIONS_SELECT =
            new PreMappedLang(GUI, "dummy.animations.select", "Select Animations Resource");
    public static final PreMappedLang GUI_DUMMY_SELECTED_ANIMATION =
            new PreMappedLang(GUI, "dummy.selected_animation", "Animation:");
    public static final PreMappedLang GUI_DUMMY_SELECTED_ANIMATION_SELECT =
            new PreMappedLang(GUI, "dummy.selected_animation.select", "Select Animation");
    public static final PreMappedLang GUI_DUMMY_ROTATION =
            new PreMappedLang(GUI, "dummy.rotation", "Rotation:");

    public static void provideFor(LanguageProvider provider) {
        for (PreMappedLang lang : LANGS_DATA) {
            provider.add(lang.key, lang.english);
        }
    }

    public static final class PreMappedLang {
        private final String key;
        private final String english;

        private PreMappedLang(String category, String key, String english) {
            this.key = String.format("%s.%s.%s", category, MOD_ID, key);
            this.english = english;
            if (DatagenModLoader.isRunningDataGen()) {
                LANGS_DATA.add(this);
            }
        }

        public TranslationTextComponent asText(Object... args) {
            return new TranslationTextComponent(key, args);
        }
    }
}
