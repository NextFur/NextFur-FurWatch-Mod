package net.nextfur.fwc.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class SignalBlockerClientHelper {
    public static void openScreen(BlockPos pos, int radius, int redstoneMode, boolean manualEnabled, boolean active) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && (mc.player.isCreative() || mc.player.hasPermissions(2))) {
            mc.setScreen(new SignalBlockerScreen(pos, radius, redstoneMode, manualEnabled, active));
        }
    }
}
