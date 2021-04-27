package denimred.simplemuseum.common.init;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;

import denimred.simplemuseum.common.command.PuppetCommand;

public class MuseumCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(PuppetCommand.create());
    }
}
