package denimred.simplemuseum.client.util;

import java.util.ArrayList;
import java.util.List;

public final class SavableHolder {
    private final List<ISavable<?>> savables = new ArrayList<>();

    @SuppressWarnings("unchecked")
    private static <T> void forceLoad(ISavable<T> savable, Object o) {
        try {
            savable.load((T) o);
        } catch (ClassCastException e) {
            throw new IllegalStateException("Failed to load savables due to type mismatch", e);
        }
    }

    public <T extends ISavable<?>> T add(T savable) {
        savables.add(savable);
        return savable;
    }

    public Object[] save() {
        final int size = savables.size();
        final Object[] data = new Object[size];
        for (int i = 0; i < size; i++) {
            data[i] = savables.get(i).save();
        }
        return data;
    }

    public void load(Object[] data) {
        for (int i = 0; i < data.length; i++) {
            forceLoad(savables.get(i), data[i]);
        }
    }
}
