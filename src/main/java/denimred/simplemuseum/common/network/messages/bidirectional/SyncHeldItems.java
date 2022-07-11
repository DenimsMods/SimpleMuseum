package denimred.simplemuseum.common.network.messages.bidirectional;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.item.HeldItemStack;
import denimred.simplemuseum.common.network.NetworkUtil;

public class SyncHeldItems {
    private final int puppetId;
    private final HashMap<String, HeldItemStack> heldItems;

    public SyncHeldItems(int id, HashMap<String, HeldItemStack> items) {
        this.puppetId = id;
        this.heldItems = items;
    }

    public static SyncHeldItems decode(FriendlyByteBuf buf) {
        final int id = buf.readInt();
        int length = buf.readInt();
        final HashMap<String, HeldItemStack> heldItems = new HashMap<>();
        for(int i = 0; i < length; i++) {
            heldItems.put(buf.readUtf(), HeldItemStack.readFromBuf(buf));
        }
        return new SyncHeldItems(id, heldItems);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(puppetId);
        buf.writeInt(heldItems.size());
        for(Map.Entry<String, HeldItemStack> pair : heldItems.entrySet()) {
            buf.writeUtf(pair.getKey());
            pair.getValue().writeToBuf(buf);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> sup) {
        final NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> doWork(ctx));
        ctx.setPacketHandled(true);
    }

    @SuppressWarnings("deprecation")
    private void doWork(NetworkEvent.Context ctx) {
        NetworkUtil.getValidPuppet(ctx, puppetId).ifPresent(puppet -> processPuppet(ctx, puppet));
    }

    private void processPuppet(NetworkEvent.Context ctx, PuppetEntity puppet) {
        if(puppet.level.isClientSide && ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            puppet.setHeldItems(this.heldItems);
        }
        else {
            // TODO: Do permissions check?
            final ServerPlayer sender = ctx.getSender();
            puppet.setHeldItems(this.heldItems);
        }
    }

}
