package denimred.simplemuseum.common.init;

import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.data.LanguageProvider;

import denimred.simplemuseum.SimpleMuseum;

public enum MuseumLang {
    GUI_CLIPBOARD_COPY("gui", "clipboard.copy", "Copy to Clipboard"),
    GUI_CLIPBOARD_PASTE("gui", "clipboard.paste", "Paste from Clipboard"),
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
    GUI_PUPPET_CONFIG("gui", "puppet.config", "Configure Puppet"),
    GUI_PUPPET_CONFIG_ANIM("gui", "puppet.config.anim", "Animation:"),
    GUI_PUPPET_CONFIG_ANIM_SELECT("gui", "puppet.config.anim.select", "Select Animation"),
    GUI_PUPPET_CONFIG_ANIMS("gui", "puppet.config.anims", "Animations Resource"),
    GUI_PUPPET_CONFIG_ANIMS_SELECT(
            "gui", "puppet.config.anims.select", "Select Animations Resource"),
    GUI_PUPPET_CONFIG_MODEL("gui", "puppet.config.model", "Model Resource"),
    GUI_PUPPET_CONFIG_MODEL_SELECT("gui", "puppet.config.model.select", "Select Model Resource"),
    GUI_PUPPET_CONFIG_TEX("gui", "puppet.config.tex", "Texture Resource"),
    GUI_PUPPET_CONFIG_TEX_SELECT("gui", "puppet.config.tex.select", "Select Texture Resource"),
    GUI_PUPPET_CONFIG_TITLE("gui", "puppet.config.title", "Configure - %s"),
    GUI_PUPPET_MOVE("gui", "puppet.move", "Move Puppet"),
    GUI_PUPPET_MOVE_APPLY("gui", "puppet.move.apply", "Apply"),
    GUI_PUPPET_MOVE_RESET("gui", "puppet.move.reset", "Reset"),
    GUI_PUPPET_MOVE_TITLE("gui", "puppet.move.title", "Move - %s"),
    GUI_PUPPET_MOVE_X("gui", "puppet.move.x", "X:"),
    GUI_PUPPET_MOVE_Y("gui", "puppet.move.y", "Y:"),
    GUI_PUPPET_MOVE_YAW("gui", "puppet.move.yaw", "Yaw:"),
    GUI_PUPPET_MOVE_Z("gui", "puppet.move.z", "Z:"),
    GUI_SEARCH("gui", "search", "Search"),
    KEY_CATEGORY("key", "category", "Simple Museum"),
    KEY_GLOBAL_HIGHLIGHTS("key", "global_highlights", "Toggle Global Highlights");

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

    public String getKey() {
        return key;
    }

    public TranslationTextComponent asText(Object... args) {
        return new TranslationTextComponent(key, args);
    }
}
