package denimred.simplemuseum.client.util;

import net.minecraft.client.Minecraft;

import denimred.simplemuseum.client.gui.screen.MuseumDummyScreen;
import denimred.simplemuseum.common.entity.MuseumDummyEntity;

public class ClientUtil {
    // TODO: This is a hotfix to fix classloading issues, consider moving it
    public static void openDummyGui(MuseumDummyEntity dummy) {
        Minecraft.getInstance().displayGuiScreen(new MuseumDummyScreen(dummy));
    }
}
