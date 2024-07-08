package dev.denimred.simplemuseum.puppet;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.denimred.simplemuseum.init.SMEntityTypes;
import dev.denimred.simplemuseum.init.SMPuppetFacets;
import dev.denimred.simplemuseum.puppet.data.PuppetFacet;
import dev.denimred.simplemuseum.puppet.entity.Puppet;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dev.denimred.simplemuseum.SimpleMuseum.LOGGER;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.EntityArgument.entities;
import static net.minecraft.commands.arguments.NbtTagArgument.nbtTag;
import static net.minecraft.commands.arguments.ResourceArgument.resource;

public final class PuppetCommands {
    public static final String PUPPET = "puppet";
    public static final String PUPPETS = "puppets";
    public static final String FACET = "facet";
    public static final String VALUE = "value";

    public static final String SET_SINGLE = "commands.simplemuseum.puppet.facet.set.single";
    public static final String SET_MULTI = "commands.simplemuseum.puppet.facet.set.multi";
    public static final String PUPPET_SELECTOR = "commands.simplemuseum.selector.puppet";
    public static final String PUPPETS_SELECTOR = "commands.simplemuseum.selector.puppets";

    @ApiStatus.Internal
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, CommandSelection selection) {
        var puppet = literal(PUPPET).requires(css -> css.hasPermission(Commands.LEVEL_GAMEMASTERS));
        var puppets = argument(PUPPETS, entities());
        var facet = argument(FACET, resource(ctx, SMPuppetFacets.REGISTRY_KEY)).executes(PuppetCommands::cmdGetFacet);
        var value = argument(VALUE, nbtTag()).executes(PuppetCommands::cmdSetFacet);
        dispatcher.register(puppet.then(puppets.then(facet.then(value))));
        LOGGER.debug("Registered Commands");
    }

    private static int cmdGetFacet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var source = ctx.getSource();
        var puppets = getPuppets(ctx);
        var value = ResourceArgument.getResource(ctx, FACET, SMPuppetFacets.REGISTRY_KEY).value();
        for (Puppet puppet : puppets) {
            var stringValue = puppet.facets().getValue(value).toString();
            source.sendSuccess(() -> Component.literal(stringValue), false);
        }
        return puppets.size();
    }

    private static int cmdSetFacet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var source = ctx.getSource();
        var puppets = getPuppets(ctx);
        var value = ResourceArgument.getResource(ctx, FACET, SMPuppetFacets.REGISTRY_KEY).value();
        var newValue = NbtTagArgument.getNbtTag(ctx, VALUE);
        var successes = puppets.stream().mapToInt(puppet -> writeFacet(puppet, value, newValue, error -> source.sendFailure(Component.literal(error)))).sum();
        var key = successes == 1 ? SET_SINGLE : SET_MULTI;
        source.sendSuccess((() -> Component.translatable(key, value, newValue, successes)), true);
        return successes;
    }

    private static List<Puppet> getPuppets(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var entities = EntityArgument.getEntities(ctx, PUPPETS);
        var puppets = new ArrayList<Puppet>(entities.size());
        for (Entity entity : entities) if (entity instanceof Puppet puppet) puppets.add(puppet);
        if (puppets.isEmpty()) throw EntityArgument.NO_ENTITIES_FOUND.create();
        return puppets;
    }

    private static <T> int writeFacet(Puppet puppet, PuppetFacet<T> facet, Tag valueTag, Consumer<String> onError) {
        var instance = puppet.facets().getInstance(facet);
        var value = facet.load(valueTag, onError);
        value.ifPresent(instance::setValue);
        return value.isPresent() ? 1 : 0;
    }

    @ApiStatus.Internal
    public static boolean parseCustomSelectors(EntitySelectorParser parser) {
        var reader = parser.getReader();
        if (!reader.canRead()) return false;
        var cursor = reader.getCursor();
        return switch (reader.readUnquotedString()) {
            case PUPPET -> {
                parser.setMaxResults(1);
                parser.setIncludesEntities(true);
                parser.setOrder(EntitySelectorParser.ORDER_NEAREST);
                parser.limitToType(SMEntityTypes.PUPPET);
                yield true;
            }
            case PUPPETS -> {
                parser.setMaxResults(Integer.MAX_VALUE);
                parser.setIncludesEntities(true);
                parser.setOrder(EntitySelector.ORDER_ARBITRARY);
                parser.limitToType(SMEntityTypes.PUPPET);
                yield true;
            }
            default -> {
                reader.setCursor(cursor);
                yield false;
            }
        };
    }

    @ApiStatus.Internal
    public static void fillCustomSelectorSuggestions(SuggestionsBuilder builder) {
        builder.suggest("@" + PUPPET, Component.translatable(PUPPET_SELECTOR));
        builder.suggest("@" + PUPPETS, Component.translatable(PUPPETS_SELECTOR));
    }
}
