package denimred.simplemuseum.common.network.messages.bidirectional;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Supplier;

import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.i18n.lang.MiscLang;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.NetworkUtil;

public final class CopyPastePuppetData {
    private static final int REQUEST_COPY = 0;
    private static final int REQUEST_PASTE = 1;
    private static final int SEND_CLIPBOARD = 2;
    private final int puppetId;
    private final int state;
    private final String clipboard;

    private CopyPastePuppetData(int puppetId, int state) {
        this(puppetId, state, "");
    }

    private CopyPastePuppetData(int puppetId, int state, String clipboard) {
        this.puppetId = puppetId;
        this.state = state;
        this.clipboard = clipboard;
    }

    public static CopyPastePuppetData copy(PuppetEntity puppet) {
        return new CopyPastePuppetData(puppet.getEntityId(), REQUEST_COPY);
    }

    public static CopyPastePuppetData paste(PuppetEntity puppet) {
        return new CopyPastePuppetData(puppet.getEntityId(), REQUEST_PASTE);
    }

    public static void register(SimpleChannel channel, int id) {
        channel.registerMessage(
                id,
                CopyPastePuppetData.class,
                CopyPastePuppetData::encode,
                CopyPastePuppetData::decode,
                CopyPastePuppetData::handle);
    }

    private static CopyPastePuppetData decode(PacketBuffer buf) {
        final int puppetId = buf.readVarInt();
        final int state = buf.readVarInt();
        final String data = state == SEND_CLIPBOARD ? buf.readString(32767) : "";
        return new CopyPastePuppetData(puppetId, state, data);
    }

    private void encode(PacketBuffer buf) {
        buf.writeVarInt(puppetId);
        buf.writeVarInt(state);
        if (state == SEND_CLIPBOARD) {
            buf.writeString(clipboard);
        }
    }

    private void handle(Supplier<NetworkEvent.Context> sup) {
        final NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> doWork(ctx));
        ctx.setPacketHandled(true);
    }

    private void doWork(NetworkEvent.Context ctx) {
        NetworkUtil.getValidPuppet(ctx, puppetId).ifPresent(puppet -> processPuppet(ctx, puppet));
    }

    private void processPuppet(NetworkEvent.Context ctx, PuppetEntity puppet) {
        if (puppet.world.isRemote) {
            if (state == REQUEST_COPY) {
                final CompoundNBT tag = new CompoundNBT();
                puppet.writeModTag(tag);
                ClientUtil.MC.keyboardListener.setClipboardString(tag.toString());
                if (ClientUtil.MC.player != null) {
                    ClientUtil.MC
                            .player
                            .getCommandSource()
                            .sendFeedback(
                                    MiscLang.COMMAND_FEEDBACK_PUPPET_COPY.asText(
                                            puppet.getDisplayName()),
                                    false);
                }
            } else if (state == REQUEST_PASTE) {
                final String data = ClientUtil.MC.keyboardListener.getClipboardString();
                MuseumNetworking.CHANNEL.reply(
                        new CopyPastePuppetData(puppet.getEntityId(), SEND_CLIPBOARD, data), ctx);
            }
        } else if (state == SEND_CLIPBOARD) {
            final ServerPlayerEntity player = ctx.getSender();
            if (player != null) {
                final CommandSource source = player.getCommandSource();
                try {
                    puppet.readModTag(JsonToNBT.getTagFromJson(clipboard));
                    source.sendFeedback(
                            MiscLang.COMMAND_FEEDBACK_PUPPET_PASTE.asText(puppet.getDisplayName()),
                            true);
                } catch (CommandSyntaxException e) {
                    source.sendErrorMessage(new StringTextComponent(e.getMessage()));
                }
            }
        }
    }
}
