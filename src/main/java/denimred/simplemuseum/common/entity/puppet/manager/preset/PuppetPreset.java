package denimred.simplemuseum.common.entity.puppet.manager.preset;

import java.util.Map;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class PuppetPreset {
    private final Map<PuppetKey, ?> data = new Object2ObjectOpenHashMap<>();

    public boolean validate() {
        for (PuppetKey key : data.keySet()) {
            if (!PuppetValueProvider.get(key).isPresent()) {
                return false;
            }
        }
        return true;
    }

    public void apply(PuppetEntity puppet) {
        for (Map.Entry<PuppetKey, ?> entry : data.entrySet()) {
            final PuppetKey key = entry.getKey();
            final Object value = entry.getValue();
            puppet.getValue(key).ifPresent(v -> v.trySet(value));
        }
    }
}
