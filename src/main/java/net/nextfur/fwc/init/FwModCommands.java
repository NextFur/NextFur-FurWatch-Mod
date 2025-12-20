package net.nextfur.fwc.init;

import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.nextfur.fwc.commands.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FwModCommands {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void register(ServerStartingEvent event) {
        OffRpCommand.register(event.getServer().getCommands().getDispatcher()); //offrp
        TitleMenuCommand.register(event.getServer().getCommands().getDispatcher()); //tmenu
        SkyColorCommand.register(event.getServer().getCommands().getDispatcher()); //skycolor
    }
}
