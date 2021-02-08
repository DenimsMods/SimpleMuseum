package denimred.simplemuseum.common.init;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.settings.ToggleableKeyBinding;

import org.lwjgl.glfw.GLFW;

public class MuseumKeybinds {
    public static final KeyBinding GLOBAL_HIGHLIGHTS =
            new ToggleableKeyBinding(
                    MuseumLang.KEY_GLOBAL_HIGHLIGHTS.getKey(),
                    GLFW.GLFW_KEY_H,
                    MuseumLang.KEY_CATEGORY.getKey(),
                    () -> true);
}
