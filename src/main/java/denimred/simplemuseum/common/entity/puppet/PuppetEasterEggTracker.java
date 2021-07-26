package denimred.simplemuseum.common.entity.puppet;

import net.minecraft.util.ResourceLocation;

import java.util.EnumSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import denimred.simplemuseum.SimpleMuseum;

public final class PuppetEasterEggTracker {
    public static final ResourceLocation ERROR_MODEL =
            new ResourceLocation(SimpleMuseum.MOD_ID, "geo/misc/error.geo.json");
    public static final ResourceLocation ERROR_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/misc/blank.png");
    private final PuppetEntity puppet;
    private final EnumSet<Egg> eggs = EnumSet.noneOf(Egg.class);

    protected PuppetEasterEggTracker(PuppetEntity puppet) {
        this.puppet = puppet;
    }

    protected void tick() {
        for (Egg egg : Egg.values()) {
            if (egg.isActive(puppet)) {
                eggs.add(egg);
            } else {
                eggs.remove(egg);
            }
        }
    }

    public boolean isActive(Egg egg) {
        // todo fix the other easter eggs
        return egg == Egg.HELLO_HOW_R_U && eggs.contains(egg);
    }

    public enum Egg {
        ERROR(
                puppet ->
                        !puppet.sourceManager.model.isValid()
                                && !puppet.sourceManager.texture.isValid()),
        HELLO_HOW_R_U(":\\)"),
        HAT_KID("(?i:Hat Kid)"),
        MICHIGAN_RAG("(?i:Michigan (J\\.? )?(Frog|Rag))");

        private final Predicate<PuppetEntity> checker;

        Egg(String regex) {
            final Pattern pattern = Pattern.compile(regex);
            this.checker =
                    puppet -> {
                        if (puppet.hasCustomName()) {
                            final String name = puppet.getName().getUnformattedComponentText();
                            return pattern.matcher(name).matches();
                        }
                        return false;
                    };
        }

        Egg(Predicate<PuppetEntity> checker) {
            this.checker = checker;
        }

        public boolean isActive(PuppetEntity puppet) {
            return checker.test(puppet);
        }
    }
}
