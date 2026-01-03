package net.nextfur.fwc.init;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.nextfur.fwc.commands.*;

public class FwModCommands {
    public static void register(ServerStartingEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getServer().getCommands().getDispatcher();

        OffRpCommand.register(dispatcher);          // offrp
        TitleMenuCommand.register(dispatcher);      // tmenu
        SkyColorCommand.register(dispatcher);       // skycolor
        DiceRollMenuCommand.register(dispatcher);   // roll
    }
}
