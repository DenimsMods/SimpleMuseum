package denimred.simplemuseum.common.entity.puppet.manager.value;

import java.util.Objects;

public final class PuppetKey implements Comparable<PuppetKey> {
    public final String manager;
    public final String provider;

    public PuppetKey(String combined) {
        this(split(combined));
    }

    public PuppetKey(String[] split) {
        this(split[0], split[1]);
    }

    public PuppetKey(String manager, String provider) {
        if (manager.isEmpty()) {
            throw new IllegalArgumentException("Manager key must not be empty");
        } else if (provider.isEmpty()) {
            throw new IllegalArgumentException("Provider key must not be empty");
        } else {
            this.manager = manager;
            this.provider = provider;
        }
    }

    private static String[] split(String combined) {
        final String[] split = new String[] {"", ""};
        final int i = combined.indexOf(':');
        if (i >= 0) {
            split[1] = combined.substring(i + 1);
            if (i >= 1) {
                split[0] = combined.substring(0, i);
            }
        }
        return split;
    }

    @Override
    public int compareTo(PuppetKey o) {
        final int i = manager.compareTo(o.manager);
        if (i != 0) {
            return i;
        } else {
            return provider.compareTo(o.provider);
        }
    }

    @Override
    public String toString() {
        return manager + ":" + provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PuppetKey puppetKey = (PuppetKey) o;
        return manager.equals(puppetKey.manager) && provider.equals(puppetKey.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(manager, provider);
    }
}
