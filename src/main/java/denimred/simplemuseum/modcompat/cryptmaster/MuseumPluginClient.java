package denimred.simplemuseum.modcompat.cryptmaster;

import java.util.Collections;
import java.util.List;

import cryptcraft.cryptmaster.api.client.ICryptMasterClientPlugin;
import cryptcraft.cryptmaster.api.client.UtilityTool;

public class MuseumPluginClient implements ICryptMasterClientPlugin {
    @Override
    public List<UtilityTool> getUtilityTools() {
        return Collections.singletonList(MuseumToolBehavior.TOOL_INSTANCE);
    }
}
