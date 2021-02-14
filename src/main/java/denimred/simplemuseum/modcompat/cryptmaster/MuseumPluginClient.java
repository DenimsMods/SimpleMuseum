package denimred.simplemuseum.modcompat.cryptmaster;

import java.util.Collections;
import java.util.List;

import cryptcraft.cryptmaster.plugin.client.ICryptMasterClientPlugin;
import cryptcraft.cryptmaster.plugin.client.UtilityTool;

public class MuseumPluginClient implements ICryptMasterClientPlugin {
    @Override
    public List<UtilityTool> getUtilityTools() {
        return Collections.singletonList(MuseumTool.INSTANCE);
    }
}
