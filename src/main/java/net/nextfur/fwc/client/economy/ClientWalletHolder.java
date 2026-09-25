package net.nextfur.fwc.client.economy;

import net.minecraft.world.item.ItemStack;

public class ClientWalletHolder {
    private static ItemStack equippedWallet = ItemStack.EMPTY;

    public static ItemStack getEquippedWallet() {
        return equippedWallet;
    }

    public static void setEquippedWallet(ItemStack stack) {
        equippedWallet = stack == null ? ItemStack.EMPTY : stack;
    }

    public static boolean hasWallet() {
        return !equippedWallet.isEmpty();
    }
}
