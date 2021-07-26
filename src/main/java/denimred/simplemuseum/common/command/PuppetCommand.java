package denimred.simplemuseum.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.i18n.lang.MiscLang;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.messages.bidirectional.CopyPastePuppetData;
import denimred.simplemuseum.common.network.messages.s2c.PlayPuppetAnimation;

public final class PuppetCommand {
    public static final SimpleCommandExceptionType NOT_A_PUPPET =
            new SimpleCommandExceptionType(MiscLang.COMMAND_EXCEPTION_NOT_A_PUPPET.asText());

    public static LiteralArgumentBuilder<CommandSource> create() {
        return Commands.literal("puppet")
                .then(cmdAnimate())
                .then(cmdCopy())
                .then(cmdPaste())
                .then(cmdResurrect());
    }

    private static ArgumentBuilder<CommandSource, ?> cmdAnimate() {
        final Command<CommandSource> cmd =
                ctx -> {
                    final List<PuppetEntity> puppets = getPuppets(ctx);
                    final String animation = StringArgumentType.getString(ctx, "animation");
                    for (PuppetEntity puppet : puppets) {
                        MuseumNetworking.CHANNEL.send(
                                PacketDistributor.TRACKING_ENTITY.with(() -> puppet),
                                new PlayPuppetAnimation(puppet.getEntityId(), animation));
                    }
                    final int count = puppets.size();
                    final CommandSource source = ctx.getSource();
                    if (count == 1) {
                        source.sendFeedback(
                                MiscLang.COMMAND_FEEDBACK_PUPPET_ANIMATE_SINGLE.asText(
                                        animation, puppets.get(0).getDisplayName()),
                                true);
                    } else {
                        source.sendFeedback(
                                MiscLang.COMMAND_FEEDBACK_PUPPET_ANIMATE_MULTIPLE.asText(
                                        animation, count),
                                true);
                    }
                    return count;
                };
        return Commands.literal("animate")
                .then(
                        puppetsArgument()
                                .then(
                                        Commands.argument("animation", StringArgumentType.word())
                                                .executes(cmd)));
    }

    private static ArgumentBuilder<CommandSource, ?> cmdCopy() {
        final Command<CommandSource> cmd =
                ctx -> {
                    final ServerPlayerEntity player = ctx.getSource().asPlayer();
                    MuseumNetworking.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            CopyPastePuppetData.copy(getPuppet(ctx)));
                    return Command.SINGLE_SUCCESS;
                };
        return Commands.literal("copy").then(puppetArgument().executes(cmd));
    }

    private static ArgumentBuilder<CommandSource, ?> cmdPaste() {
        final Command<CommandSource> cmd =
                ctx -> {
                    final ServerPlayerEntity player = ctx.getSource().asPlayer();
                    MuseumNetworking.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            CopyPastePuppetData.paste(getPuppet(ctx)));
                    return Command.SINGLE_SUCCESS;
                };
        return Commands.literal("paste").then(puppetArgument().executes(cmd));
    }

    private static ArgumentBuilder<CommandSource, ?> cmdResurrect() {
        final Command<CommandSource> cmd =
                ctx -> {
                    int count = 0;
                    for (PuppetEntity puppet : getPuppets(ctx)) {
                        if (puppet.isDead()) {
                            puppet.resurrect();
                            count++;
                        }
                    }
                    ctx.getSource()
                            .sendFeedback(
                                    MiscLang.COMMAND_FEEDBACK_PUPPET_RESURRECT.asText(count), true);
                    return count;
                };
        return Commands.literal("resurrect").then(puppetsArgument().executes(cmd));
    }

    private static RequiredArgumentBuilder<CommandSource, EntitySelector> puppetArgument() {
        return Commands.argument("puppet", EntityArgument.entity());
    }

    private static PuppetEntity getPuppet(CommandContext<CommandSource> ctx)
            throws CommandSyntaxException {
        final Entity entity = EntityArgument.getEntity(ctx, "puppet");
        if (!(entity instanceof PuppetEntity)) {
            throw NOT_A_PUPPET.create();
        } else {
            return (PuppetEntity) entity;
        }
    }

    private static RequiredArgumentBuilder<CommandSource, EntitySelector> puppetsArgument() {
        return Commands.argument("puppets", EntityArgument.entities());
    }

    private static List<PuppetEntity> getPuppets(CommandContext<CommandSource> ctx)
            throws CommandSyntaxException {
        final List<PuppetEntity> puppets = new ArrayList<>();
        for (Entity entity : EntityArgument.getEntities(ctx, "puppets")) {
            if (entity instanceof PuppetEntity) {
                puppets.add((PuppetEntity) entity);
            }
        }
        if (puppets.isEmpty()) {
            throw EntityArgument.ENTITY_NOT_FOUND.create();
        }
        return puppets;
    }
}
