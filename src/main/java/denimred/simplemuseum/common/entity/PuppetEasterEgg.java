package denimred.simplemuseum.common.entity;

import java.util.function.Predicate;

public enum PuppetEasterEgg {
    HELLO_HOW_R_U(forName(":)"));

    private final Predicate<MuseumPuppetEntity> checker;

    PuppetEasterEgg(Predicate<MuseumPuppetEntity> checker) {
        this.checker = checker;
    }

    private static Predicate<MuseumPuppetEntity> forName(String name) {
        return puppet ->
                puppet.hasCustomName()
                        && puppet.getName().getUnformattedComponentText().equals(name);
    }

    public boolean isActive(MuseumPuppetEntity puppet) {
        return checker.test(puppet);
    }
}
