package denimred.simplemuseum.modcompat;

import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

import cryptcraft.cryptmaster.CryptMasterMod;
import cryptcraft.cryptmaster.forge.CryptMasterForgeMod;
import denimred.simplemuseum.modcompat.cryptmaster.MuseumPlugin;

public class ModCompatUtil {
    public static void enqueueIMC(@SuppressWarnings("unused") final InterModEnqueueEvent event) {
        // Constants are inlined at compile time
        InterModComms.sendTo(
                CryptMasterMod.MOD_ID,
                CryptMasterForgeMod.REGISTER_PLUGIN_IMC,
                new MuseumPlugin.Thing());
    }
}
