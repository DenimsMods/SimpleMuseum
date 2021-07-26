package denimred.simplemuseum.common.i18n.lang;

import denimred.simplemuseum.common.i18n.I18nUtil;
import denimred.simplemuseum.common.i18n.Translatable;

public enum GuiLang implements Translatable {
    CLIPBOARD_COPY("clipboard.copy", "Copy to Clipboard"),
    CLIPBOARD_PASTE("clipboard.paste", "Paste from Clipboard"),
    ERROR("error", "Error, check log!"),
    LOADING("loading", "Loading..."),
    MOVE_AWAY("move.away", "Move Away"),
    MOVE_CENTER("move.center", "Center on Block"),
    MOVE_DOWN("move.down", "Move Down"),
    MOVE_LEFT("move.left", "Move Left"),
    MOVE_RIGHT("move.right", "Move Right"),
    MOVE_ROTATE_CCW("move.rotate.ccw", "Rotate Counterclockwise"),
    MOVE_ROTATE_CW("move.rotate.cw", "Rotate Clockwise"),
    MOVE_TOWARDS("move.towards", "Move Towards"),
    MOVE_UP("move.up", "Move Up"),
    PUPPET_CONFIG("puppet.config", "Configure Puppet"),
    PUPPET_CONFIG_ANIM("puppet.config.anim", "Animation:"),
    PUPPET_CONFIG_ANIM_SELECT("puppet.config.anim.select", "Select Animation"),
    PUPPET_CONFIG_ANIMS("puppet.config.anims", "Animations Resource"),
    PUPPET_CONFIG_ANIMS_SELECT("puppet.config.anims.select", "Select Animations Resource"),
    PUPPET_CONFIG_MODEL("puppet.config.model", "Model Resource"),
    PUPPET_CONFIG_MODEL_SELECT("puppet.config.model.select", "Select Model Resource"),
    PUPPET_CONFIG_TEX("puppet.config.tex", "Texture Resource"),
    PUPPET_CONFIG_TEX_SELECT("puppet.config.tex.select", "Select Texture Resource"),
    PUPPET_CONFIG_TITLE("puppet.config.title", "Configure - %s"),
    PUPPET_MOVE("puppet.move", "Move Puppet"),
    PUPPET_MOVE_APPLY("puppet.move.apply", "Apply"),
    PUPPET_MOVE_RESET("puppet.move.reset", "Reset"),
    PUPPET_MOVE_TITLE("puppet.move.title", "Move - %s"),
    PUPPET_MOVE_X("puppet.move.x", "X:"),
    PUPPET_MOVE_Y("puppet.move.y", "Y:"),
    PUPPET_MOVE_PITCH("puppet.move.pitch", "Pitch:"),
    PUPPET_MOVE_YAW("puppet.move.yaw", "Yaw:"),
    PUPPET_MOVE_Z("puppet.move.z", "Z:"),
    PUPPET_PREVIEW_BOXES("puppet.preview.boxes", "Toggle Bounding Boxes"),
    PUPPET_PREVIEW_FLOOR("puppet.preview.floor", "Toggle Floor"),
    PUPPET_PREVIEW_FULLSCREEN("puppet.preview.fullscreen", "Toggle Fullscreen"),
    PUPPET_PREVIEW_PLAYER("puppet.preview.player", "Toggle Player Reference"),
    PUPPET_PREVIEW_RESET("puppet.preview.reset", "Reset Preview"),
    PUPPET_PREVIEW_TURNTABLE("puppet.preview.turntable", "Toggle Turntable"),
    SEARCH("search", "Search");

    private final String key;
    private final String english;

    GuiLang(String key, String english) {
        this.key = I18nUtil.simple("gui", key);
        this.english = english;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getEnglish() {
        return english;
    }
}
