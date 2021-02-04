package denimred.simplemuseum.common.init;

import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.data.LanguageProvider;

import denimred.simplemuseum.SimpleMuseum;

public enum MuseumLang {
    GUI_CLIPBOARD_COPY("gui", "clipboard.copy", "Copy to Clipboard"),
    GUI_CLIPBOARD_PASTE("gui", "clipboard.paste", "Paste from Clipboard"),
    GUI_DUMMY_CONFIG("gui", "dummy.config", "Configure Dummy"),
    GUI_DUMMY_CONFIG_ANIM("gui", "dummy.config.anim", "Animation:"),
    GUI_DUMMY_CONFIG_ANIM_SELECT("gui", "dummy.config.anim.select", "Select Animation"),
    GUI_DUMMY_CONFIG_ANIMS("gui", "dummy.config.anims", "Animations Resource"),
    GUI_DUMMY_CONFIG_ANIMS_SELECT("gui", "dummy.config.anims.select", "Select Animations Resource"),
    GUI_DUMMY_CONFIG_MODEL("gui", "dummy.config.model", "Model Resource"),
    GUI_DUMMY_CONFIG_MODEL_SELECT("gui", "dummy.config.model.select", "Select Model Resource"),
    GUI_DUMMY_CONFIG_TEX("gui", "dummy.config.tex", "Texture Resource"),
    GUI_DUMMY_CONFIG_TEX_SELECT("gui", "dummy.config.tex.select", "Select Texture Resource"),
    GUI_DUMMY_CONFIG_TITLE("gui", "dummy.config.title", "Configure - %s"),
    GUI_DUMMY_MOVE("gui", "dummy.move", "Move Dummy"),
    GUI_DUMMY_MOVE_APPLY("gui", "dummy.move.apply", "Apply"),
    GUI_DUMMY_MOVE_RESET("gui", "dummy.move.reset", "Reset"),
    GUI_DUMMY_MOVE_TITLE("gui", "dummy.move.title", "Move - %s"),
    GUI_DUMMY_MOVE_X("gui", "dummy.move.x", "X:"),
    GUI_DUMMY_MOVE_Y("gui", "dummy.move.y", "Y:"),
    GUI_DUMMY_MOVE_YAW("gui", "dummy.move.yaw", "Yaw:"),
    GUI_DUMMY_MOVE_Z("gui", "dummy.move.z", "Z:"),
    GUI_ERROR("gui", "error", "Error, check log!"),
    GUI_LOADING("gui", "loading", "Loading..."),
    GUI_MOVE_AWAY("gui", "move.away", "Move Away"),
    GUI_MOVE_CENTER("gui", "move.center", "Center on Block"),
    GUI_MOVE_DOWN("gui", "move.down", "Move Down"),
    GUI_MOVE_LEFT("gui", "move.left", "Move Left"),
    GUI_MOVE_RIGHT("gui", "move.right", "Move Right"),
    GUI_MOVE_ROTATE_CCW("gui", "move.rotate.ccw", "Rotate Counterclockwise"),
    GUI_MOVE_ROTATE_CW("gui", "move.rotate.cw", "Rotate Clockwise"),
    GUI_MOVE_TOWARDS("gui", "move.towards", "Move Towards"),
    GUI_MOVE_UP("gui", "move.up", "Move Up"),
    GUI_SEARCH("gui", "search", "Search");

    private final String key;
    private final String english;

    MuseumLang(String category, String key, String english) {
        this.key = String.format("%s.%s.%s", category, SimpleMuseum.MOD_ID, key);
        this.english = english;
    }

    public static void provideFor(LanguageProvider provider) {
        for (MuseumLang lang : MuseumLang.values()) {
            provider.add(lang.key, lang.english);
        }
    }

    public TranslationTextComponent asText(Object... args) {
        return new TranslationTextComponent(key, args);
    }
}
