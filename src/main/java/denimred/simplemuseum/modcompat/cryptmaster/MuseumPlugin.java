package denimred.simplemuseum.modcompat.cryptmaster;

import java.util.function.Supplier;

import cryptcraft.cryptmaster.api.ICryptMasterPlugin;
import cryptcraft.cryptmaster.api.client.ICryptMasterClientPlugin;

public class MuseumPlugin implements ICryptMasterPlugin {
    @Override
    public ICryptMasterClientPlugin createClientPlugin() {
        return new MuseumPluginClient();
    }

    // Resolves classloading problems (inner classes are flattened) in the most Java way possible
    // Called "Thing" because that's what Forge calls its param in InterModComms.sendTo()
    public static class Thing implements Supplier<Object> {
        @Override
        public Object get() {
            return new MuseumPlugin();
        }
    }
}
