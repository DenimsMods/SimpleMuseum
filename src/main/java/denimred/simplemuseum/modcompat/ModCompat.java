package denimred.simplemuseum.modcompat;

import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

public final class ModCompat {
    public static void enqueueIMC(@SuppressWarnings("unused") final InterModEnqueueEvent event) {
        CryptMaster.sendIMC();
    }

    // TODO: Re-implement CryptMaster compat
    public static final class CryptMaster {
        private static void sendIMC() {
            // compat removed
        }

        public static boolean isLoaded() {
            return false; // compat removed
        }

        public static void registerPossession() {
            // compat removed
        }

        public static boolean isActive() {
            return false; // compat removed
        }
    }
}
