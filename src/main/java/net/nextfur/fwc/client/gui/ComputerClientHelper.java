package net.nextfur.fwc.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class ComputerClientHelper {
    public static void openScreen(BlockPos pos, String url) {
        Minecraft mc = Minecraft.getInstance();
        boolean isCreative = mc.player != null && (mc.player.isCreative() || mc.player.hasPermissions(2));
        mc.setScreen(new ComputerScreen(pos, url, isCreative));
    }
}
