package denimred.simplemuseum.common.i18n.lang;

import denimred.simplemuseum.common.i18n.I18nUtil;
import denimred.simplemuseum.common.i18n.Translatable;

public enum MiscLang implements Translatable {
    COMMAND_EXCEPTION_NOT_A_PUPPET(
            "command", "exception.not_a_puppet", "Selected entity must be a Museum Puppet!"),
    COMMAND_FEEDBACK_PUPPET_ANIMATE_MULTIPLE(
            "command", "feedback.puppet.animate.multiple", "Trying to play [%s] on %d puppet(s)"),
    COMMAND_FEEDBACK_PUPPET_ANIMATE_SINGLE(
            "command", "feedback.puppet.animate.single", "Trying to play [%s] on %s"),
    COMMAND_FEEDBACK_PUPPET_COPY(
            "command", "feedback.puppet.copy", "Copied data from %s to clipboard"),
    COMMAND_FEEDBACK_PUPPET_PASTE(
            "command", "feedback.puppet.paste", "Pasted data from clipboard onto %s"),
    COMMAND_FEEDBACK_PUPPET_RESURRECT(
            "command", "feedback.puppet.resurrect", "Resurrected %d dead puppet(s)"),
    COMMAND_FEEDBACK_PUPPET_SET_INVULNERABILITY(
            "command",
            "feedback.puppet.set_invulnerability",
            "Set invulnerability for %d puppet(s) to %b"),
    KEY_CATEGORY("key", "category", "Simple Museum"),
    KEY_GLOBAL_HIGHLIGHTS("key", "global_highlights", "Toggle Global Highlights");

    private final String key;
    private final String english;

    MiscLang(String category, String key, String english) {
        this.key = I18nUtil.simple(category, key);
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
