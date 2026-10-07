package net.nextfur.fwc.pda.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.nextfur.fwc.pda.data.PdaData;

@OnlyIn(Dist.CLIENT)
public class PdaClientHelper {
    public static void openPdaScreen(ItemStack stack, boolean isMainHand) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.setScreen(new PdaScreen(stack, isMainHand));
        }
    }

    public static void syncPdaData(PdaData data) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof PdaScreen pdaScreen) {
            pdaScreen.updatePdaData(data);
        }
    }
}
