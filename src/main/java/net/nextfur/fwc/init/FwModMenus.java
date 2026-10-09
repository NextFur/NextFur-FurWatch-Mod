package net.nextfur.fwc.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.economy.menu.WalletMenu;

public class FwModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, FwMain.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<WalletMenu>> WALLET_MENU =
            MENUS.register("wallet_menu", () -> IMenuTypeExtension.create(WalletMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<net.nextfur.fwc.economy.menu.AtmMenu>> ATM_MENU =
            MENUS.register("atm_menu", () -> IMenuTypeExtension.create(net.nextfur.fwc.economy.menu.AtmMenu::new));
}
