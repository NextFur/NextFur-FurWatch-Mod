package net.nextfur.fwc.init;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.nextfur.fwc.commands.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FwModCommands {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void register(ServerStartingEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getServer().getCommands().getDispatcher();

        OffRpCommand.register(dispatcher);      // offrp
        TitleMenuCommand.register(dispatcher);  // tmenu
        SkyColorCommand.register(dispatcher);   // skycolor
        SoundMenuCommand.register(dispatcher);  // soundmenu
    }
}
