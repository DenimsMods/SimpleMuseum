package denimred.simplemuseum.common.init;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.settings.ToggleableKeyBinding;

import org.lwjgl.glfw.GLFW;

import denimred.simplemuseum.common.i18n.lang.MiscLang;

public class MuseumKeybinds {
    public static final KeyBinding GLOBAL_HIGHLIGHTS =
            new ToggleableKeyBinding(
                    MiscLang.KEY_GLOBAL_HIGHLIGHTS.getKey(),
                    GLFW.GLFW_KEY_H,
                    MiscLang.KEY_CATEGORY.getKey(),
                    () -> true);
}
