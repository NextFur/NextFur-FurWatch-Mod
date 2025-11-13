package net.nextfur.fwc.network.nextfur;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.network.nextfur.packets.AuthResponsePacket;

public class ClientAuthHandler {

    public static void onAuthRequest(IPayloadContext context) {
        String token = FwMain.CLIENT_TOKEN;
        String nextId = FwMain.CLIENT_ID;
        String username = Minecraft.getInstance().getUser().getName();

        context.reply(new AuthResponsePacket(username, token, nextId));
    }
}
