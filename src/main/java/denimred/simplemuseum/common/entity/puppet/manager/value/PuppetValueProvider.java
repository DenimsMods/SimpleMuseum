package denimred.simplemuseum.common.entity.puppet.manager.value;

import net.minecraft.network.datasync.DataParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.i18n.I18nUtil;
import denimred.simplemuseum.common.util.IValueSerializer;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

public abstract class PuppetValueProvider<
        T, V extends PuppetValue<T, ? extends PuppetValueProvider<T, V>>> {
    private static final Map<PuppetKey, PuppetValueProvider<?, ?>> PROVIDERS =
            new Object2ReferenceOpenHashMap<>();
    public final PuppetKey key;
    public final T defaultValue;
    public final IValueSerializer<T> serializer;
    @Nullable public final DataParameter<T> dataKey;
    public final String translationKey;
    @Nullable public final Callback<T> callback;

    protected PuppetValueProvider(
            PuppetKey key,
            T defaultValue,
            IValueSerializer<T> serializer,
            @Nullable Callback<T> callback) {
        this(key, defaultValue, serializer, callback, PuppetEntity.createKeyContextual(serializer));
    }

    protected PuppetValueProvider(
            PuppetKey key,
            T defaultValue,
            IValueSerializer<T> serializer,
            @Nullable Callback<T> callback,
            @Nullable DataParameter<T> dataKey) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.serializer = serializer;
        this.dataKey = dataKey;
        this.callback = callback;
        this.translationKey = I18nUtil.valueProvider(this);
        if (PROVIDERS.putIfAbsent(key, this) != null) {
            throw new IllegalArgumentException("Duplicate provider initialization for " + key);
        }
    }

    public static <T> Optional<PuppetValueProvider<T, ?>> get(PuppetKey key) {
        return Optional.ofNullable(PROVIDERS.get(key)).map(PuppetValueProvider::cast);
    }

    public static List<PuppetValueProvider<?, ?>> getAll() {
        return new ArrayList<>(PROVIDERS.values());
    }

    @SuppressWarnings("unchecked")
    public <C> PuppetValueProvider<C, ?> cast() {
        return (PuppetValueProvider<C, ?>) this;
    }

    public abstract V provideFor(PuppetValueManager manager);
}
