package dev.denimred.simplemuseum.puppet.edit;

import dev.denimred.simplemuseum.puppet.entity.Puppet;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

import static dev.denimred.simplemuseum.SimpleMuseum.id;

public final class OpenPuppetEditScreen implements FabricPacket {
    public static final PacketType<OpenPuppetEditScreen> TYPE = PacketType.create(id("open_puppet_edit_screen"), OpenPuppetEditScreen::new);
    public final int containerId;
    public final int puppetId;

    public OpenPuppetEditScreen(PuppetEditMenu menu, Puppet puppet) {
        this(menu.containerId, puppet.getId());
    }

    private OpenPuppetEditScreen(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt());
    }

    private OpenPuppetEditScreen(int containerId, int puppetId) {
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
