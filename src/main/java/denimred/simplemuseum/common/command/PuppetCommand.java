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

import denimred.simplemuseum.common.entity.MuseumPuppetEntity;
import denimred.simplemuseum.common.init.MuseumLang;
import denimred.simplemuseum.common.init.MuseumNetworking;
import denimred.simplemuseum.common.network.messages.bidirectional.CopyPastePuppetData;
import denimred.simplemuseum.common.network.messages.s2c.PlayPuppetAnimation;

public final class PuppetCommand {
    public static final SimpleCommandExceptionType NOT_A_PUPPET =
            new SimpleCommandExceptionType(MuseumLang.COMMAND_EXCEPTION_NOT_A_PUPPET.asText());
    public static final SimpleCommandExceptionType NOT_DEAD =
            new SimpleCommandExceptionType(MuseumLang.COMMAND_EXCEPTION_NOT_DEAD.asText());

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
                    final MuseumPuppetEntity puppet = getPuppet(ctx);
                    final String animation = StringArgumentType.getString(ctx, "animation");
                    MuseumNetworking.CHANNEL.send(
                            PacketDistributor.TRACKING_ENTITY.with(() -> puppet),
                            new PlayPuppetAnimation(puppet.getEntityId(), animation));
                    ctx.getSource()
                            .sendFeedback(
                                    MuseumLang.COMMAND_FEEDBACK_PUPPET_ANIMATE.asText(
                                            animation, puppet.getDisplayName()),
                                    true);
                    return Command.SINGLE_SUCCESS;
                };
        return Commands.literal("animate")
                .then(
                        puppetArgument()
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
                    final MuseumPuppetEntity puppet = getPuppet(ctx);
                    if (puppet.isDead()) {
                        puppet.resurrect();
                        return Command.SINGLE_SUCCESS;
                    } else {
                        throw NOT_DEAD.create();
                    }
                };
        return Commands.literal("resurrect").then(puppetArgument().executes(cmd));
    }

    private static RequiredArgumentBuilder<CommandSource, EntitySelector> puppetArgument() {
        return Commands.argument("puppet", EntityArgument.entity());
    }

    private static MuseumPuppetEntity getPuppet(CommandContext<CommandSource> ctx)
            throws CommandSyntaxException {
        final Entity entity = EntityArgument.getEntity(ctx, "puppet");
        if (!(entity instanceof MuseumPuppetEntity)) {
            throw NOT_A_PUPPET.create();
        } else {
            return (MuseumPuppetEntity) entity;
        }
    }
}
