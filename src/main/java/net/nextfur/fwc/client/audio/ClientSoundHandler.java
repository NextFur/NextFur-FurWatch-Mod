package net.nextfur.fwc.client.audio;

import net.minecraft.client.Minecraft;
import net.nextfur.fwc.network.world.SoundControlPacket;

public class ClientSoundHandler {

    public static void handlePacket(SoundControlPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        switch (packet.action) {
            case "PLAY":
                FwAudioEngine.play(packet.url, packet.volume, packet.radius, packet.loop);
                break;

            case "PAUSE":
                FwAudioEngine.setPaused(packet.url, true);
                break;

            case "RESUME":
                FwAudioEngine.setPaused(packet.url, false);
                break;

            case "STOP":
                FwAudioEngine.stop(packet.url);
                break;
        }
    }

}
