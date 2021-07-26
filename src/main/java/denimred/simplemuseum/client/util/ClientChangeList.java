package denimred.simplemuseum.client.util;

import com.mojang.datafixers.util.Pair;

import net.minecraft.util.text.Color;
import net.minecraft.util.text.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

public class ClientChangeList {
    private final Map<PuppetValue<?, ?>, Entry<?>> changes = new Reference2ObjectOpenHashMap<>();

    public <T> Entry<T> entry(PuppetValue<T, ?> value) {
        final Entry<T> entry = new Entry<>(value);
        changes.put(value, entry);
        return entry;
    }

    public List<Pair<PuppetKey, ?>> compile() {
        final List<Pair<PuppetKey, ?>> changes = new ArrayList<>();
        for (Entry<?> entry : this.changes.values()) {
            if (entry.hasChanged()) {
                changes.add(Pair.of(entry.binding.provider.key, entry.binding.get()));
            }
        }
        return changes;
    }

    public static class Entry<T> implements Supplier<T> {
        public static final Color VALID_UNCHANGED = Color.fromInt(0xE0E0E0);
        public static final Color VALID_CHANGED = Color.fromInt(0xFFFFFF);
        public static final Color VALID_DISABLED = Color.fromInt(0x707070);
        public static final Color INVALID_UNCHANGED = Color.fromInt(0xE0E000);
        public static final Color INVALID_CHANGED = Color.fromInt(0xFFFF00);
        public static final Color INVALID_DISABLED = Color.fromInt(0x707000);
        public static final Color ERROR = Color.fromInt(0xFF0000);
        private final PuppetValue<T, ?> binding;
        private final T initial;
        private boolean hasErrors;
        private boolean hasChanged;
        private boolean isValid;

        private Entry(PuppetValue<T, ?> binding) {
            this.binding = binding;
            this.initial = binding.get();
        }

        @Override
        public T get() {
            return binding.get();
        }

        public void trySet(Supplier<T> sup) {
            try {
                this.set(sup.get());
                hasErrors = false;
            } catch (Throwable t) {
                hasErrors = true;
            }
        }

        public void set(T value) {
            binding.set(value);
            hasChanged = !value.equals(initial);
            isValid = binding.test(value);
        }

        public void revert() {
            this.set(initial);
        }

        public boolean isValid() {
            return isValid;
        }

        public boolean hasChanged() {
            return hasChanged;
        }

        public Style getStyle(boolean disabled) {
            return Style.EMPTY.setItalic(hasChanged).setColor(this.getColor(disabled));
        }

        public Color getColor(boolean disabled) {
            // :)
            return hasErrors
                    ? ERROR
                    : isValid
                            ? disabled
                                    ? VALID_DISABLED
                                    : hasChanged ? VALID_CHANGED : VALID_UNCHANGED
                            : disabled
                                    ? INVALID_DISABLED
                                    : hasChanged ? INVALID_CHANGED : INVALID_UNCHANGED;
        }
    }
}
