package net.nextfur.fwc.init;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.nextfur.fwc.commands.*;

public class FwModCommands {
    public static void register(ServerStartingEvent event) {
        registerCommands(event.getServer().getCommands().getDispatcher());
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Disable vanilla tell, msg, and w
        CommandRemover.removeCommand(dispatcher.getRoot(), "tell");
        CommandRemover.removeCommand(dispatcher.getRoot(), "msg");
        CommandRemover.removeCommand(dispatcher.getRoot(), "w");

        // Register mod commands
        PdaTellCommand.register(dispatcher);        // tell, msg, w
        OffRpCommand.register(dispatcher);          // offrp
        TitleMenuCommand.register(dispatcher);      // tmenu
        SkyColorCommand.register(dispatcher);       // skycolor
        DiceRollMenuCommand.register(dispatcher);   // roll
        EconomyCommand.register(dispatcher);        // economy, fweco, carteira
    }
}
