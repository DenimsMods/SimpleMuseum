package dev.denimred.simplemuseum.puppet.gui.subscreen;

import dev.denimred.simplemuseum.init.SMPuppetFacetGroups;
import dev.denimred.simplemuseum.puppet.data.PuppetFacetGroup;

public final class EmptySubscreen extends Subscreen {
    public static final EmptySubscreen BLANK = new EmptySubscreen(SMPuppetFacetGroups.BLANK, PuppetFacetGroup.ROOT_SECTION);

    public EmptySubscreen(PuppetFacetGroup group, String section) {
        super(group, section);
    }
}
