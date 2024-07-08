package dev.denimred.simplemuseum.puppet.edit;

import dev.denimred.simplemuseum.puppet.entity.Puppet;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

import static dev.denimred.simplemuseum.SimpleMuseum.id;

public final class OpenPuppetFacetEditScreen implements FabricPacket {
    public static final PacketType<OpenPuppetFacetEditScreen> TYPE = PacketType.create(id("open_puppet_facet_edit_screen"), OpenPuppetFacetEditScreen::new);
    public final int containerId;
    public final int puppetId;

    public OpenPuppetFacetEditScreen(PuppetFacetEditMenu menu, Puppet puppet) {
        this(menu.containerId, puppet.getId());
    }

    private OpenPuppetFacetEditScreen(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt());
    }

    private OpenPuppetFacetEditScreen(int containerId, int puppetId) {
        this.containerId = containerId;
        this.puppetId = puppetId;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeVarInt(puppetId);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
