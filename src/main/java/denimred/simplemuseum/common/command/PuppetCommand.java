package denimred.simplemuseum.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

import denimred.simplemuseum.common.entity.MuseumDummyEntity;

public final class PuppetCommand {
    public static LiteralArgumentBuilder<CommandSource> create() {
        return Commands.literal("puppet")
                .then(cmdSetInvulnerability());
    }

    private static ArgumentBuilder<CommandSource, ?> cmdSetInvulnerability() {
        final String invulnerableArg = "invulnerable";
        final Command<CommandSource> cmd =
                ctx -> {
                    final boolean invincible = ctx.getArgument(invulnerableArg, Boolean.class);
                    int count = 0;
                    for (MuseumDummyEntity puppet : getPuppets(ctx)) {
                        if (puppet.isInvulnerable() != invincible) {
                            puppet.setInvulnerable(invincible);
                            count++;
                        }
                    }
                    ctx.getSource()
                            .sendFeedback(
                                    new StringTextComponent(String.format("Set invulnerability for %d puppet(s) to %b", count, invincible)),
                                    true);
                    return count;
                };
        return Commands.literal("setInvulnerability").then(puppetsArgument().then(Commands.argument(invulnerableArg, BoolArgumentType.bool()).executes(cmd)));
    }

    private static RequiredArgumentBuilder<CommandSource, EntitySelector> puppetsArgument() {
        return Commands.argument("puppets", EntityArgument.entities());
    }

    private static List<MuseumDummyEntity> getPuppets(CommandContext<CommandSource> ctx)
            throws CommandSyntaxException {
        final List<MuseumDummyEntity> puppets = new ArrayList<>();
        for (Entity entity : EntityArgument.getEntities(ctx, "puppets")) {
            if (entity instanceof MuseumDummyEntity) {
                puppets.add((MuseumDummyEntity) entity);
            }
        }
        if (puppets.isEmpty()) {
            throw EntityArgument.ENTITY_NOT_FOUND.create();
        }
        return puppets;
    }
}
