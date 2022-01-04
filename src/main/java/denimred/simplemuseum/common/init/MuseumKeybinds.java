package denimred.simplemuseum.common.init;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.ToggleKeyMapping;

import org.lwjgl.glfw.GLFW;

import denimred.simplemuseum.common.i18n.lang.MiscLang;

public class MuseumKeybinds {
    public static final KeyMapping GLOBAL_HIGHLIGHTS =
            new ToggleKeyMapping(
                    MiscLang.KEY_GLOBAL_HIGHLIGHTS.getKey(),
                    GLFW.GLFW_KEY_H,
                    MiscLang.KEY_CATEGORY.getKey(),
                    () -> true);
}
