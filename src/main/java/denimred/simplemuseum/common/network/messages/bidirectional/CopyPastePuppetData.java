package denimred.simplemuseum.common.network.messages.bidirectional;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.i18n.lang.MiscLang;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.NetworkUtil;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

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
        return new CopyPastePuppetData(puppet.getId(), REQUEST_COPY);
    }

    public static CopyPastePuppetData paste(PuppetEntity puppet) {
        return new CopyPastePuppetData(puppet.getId(), REQUEST_PASTE);
    }

    public static void register(SimpleChannel channel, int id) {
        channel.registerMessage(
                id,
                CopyPastePuppetData.class,
                CopyPastePuppetData::encode,
                CopyPastePuppetData::decode,
                CopyPastePuppetData::handle);
    }

    private static CopyPastePuppetData decode(FriendlyByteBuf buf) {
        final int puppetId = buf.readVarInt();
        final int state = buf.readVarInt();
        final String data = state == SEND_CLIPBOARD ? buf.readUtf(32767) : "";
        return new CopyPastePuppetData(puppetId, state, data);
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(puppetId);
        buf.writeVarInt(state);
        if (state == SEND_CLIPBOARD) {
            buf.writeUtf(clipboard);
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
        if (puppet.level.isClientSide) {
            if (state == REQUEST_COPY) {
                final CompoundTag tag = new CompoundTag();
                puppet.writeModTag(tag);
                ClientUtil.MC.keyboardHandler.setClipboard(tag.toString());
                if (ClientUtil.MC.player != null) {
                    ClientUtil.MC
                            .player
                            .createCommandSourceStack()
                            .sendSuccess(
                                    MiscLang.COMMAND_FEEDBACK_PUPPET_COPY.asText(
                                            puppet.getDisplayName()),
                                    false);
                }
            } else if (state == REQUEST_PASTE) {
                final String data = ClientUtil.MC.keyboardHandler.getClipboard();
                MuseumNetworking.CHANNEL.reply(
                        new CopyPastePuppetData(puppet.getId(), SEND_CLIPBOARD, data), ctx);
            }
        } else if (state == SEND_CLIPBOARD) {
            final ServerPlayer player = ctx.getSender();
            if (player != null) {
                final CommandSourceStack source = player.createCommandSourceStack();
                try {
                    puppet.readModTag(TagParser.parseTag(clipboard));
                    source.sendSuccess(
                            MiscLang.COMMAND_FEEDBACK_PUPPET_PASTE.asText(puppet.getDisplayName()),
                            true);
                } catch (CommandSyntaxException e) {
                    source.sendFailure(new TextComponent(e.getMessage()));
                }
            }
        }
    }
}
