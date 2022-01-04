package denimred.simplemuseum.modcompat;

import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

import cryptcraft.cryptgui.CryptGuiScreen;
import cryptcraft.cryptmaster.CryptMasterMod;
import cryptcraft.cryptmaster.forge.CryptMasterForgeMod;
import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.modcompat.cryptmaster.MuseumPlugin;
import denimred.simplemuseum.modcompat.cryptmaster.PuppetPossessableBehavior;

public final class ModCompat {
    public static void enqueueIMC(@SuppressWarnings("unused") final InterModEnqueueEvent event) {
        CryptMaster.sendIMC();
    }

    public static final class CryptMaster {
        private static void sendIMC() {
            // Constants are inlined at compile time
            InterModComms.sendTo(
                    CryptMasterMod.MOD_ID,
                    CryptMasterForgeMod.REGISTER_PLUGIN_IMC,
                    new MuseumPlugin.Thing());
        }

        public static boolean isLoaded() {
            return ModList.get().isLoaded(CryptMasterMod.MOD_ID);
        }

        public static void registerPossession() {
            if (isLoaded()) {
                PuppetPossessableBehavior.register();
            }
        }

        public static boolean isActive() {
            return isLoaded() && ClientUtil.MC.screen instanceof CryptGuiScreen;
        }
    }
}
