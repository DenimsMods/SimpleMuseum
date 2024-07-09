package dev.denimred.simplemuseum.puppet.gui.subscreen;

import dev.denimred.simplemuseum.puppet.data.PuppetFacetGroup;

public interface SubscreenFactory {
    Subscreen createSubscreen(PuppetFacetGroup group, String section);
}
