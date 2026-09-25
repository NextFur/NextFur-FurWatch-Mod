package net.nextfur.fwc.init;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;

import java.util.List;

public class FwModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FwMain.MODID);

    public static final DeferredItem<Item> SCRAP_METAL = ITEMS.register(
        "scrap_metal",
        () -> new Item(new Item.Properties())
    );

    public static final DeferredItem<Item> LUXMARK_COIN = ITEMS.register(
            "luxmark_coin",
            () -> new Item(new Item.Properties())
    );

    // Economy - Wallet & Checks
    public static final DeferredItem<net.nextfur.fwc.economy.items.WalletItem> WALLET = ITEMS.register(
            "wallet",
            () -> new net.nextfur.fwc.economy.items.WalletItem(new Item.Properties())
    );

    public static final DeferredItem<net.nextfur.fwc.economy.items.BlankCheckItem> BLANK_CHECK = ITEMS.register(
            "blank_check",
            () -> new net.nextfur.fwc.economy.items.BlankCheckItem(new Item.Properties())
    );

    public static final DeferredItem<net.nextfur.fwc.economy.items.SignedCheckItem> SIGNED_CHECK = ITEMS.register(
            "signed_check",
            () -> new net.nextfur.fwc.economy.items.SignedCheckItem(new Item.Properties())
    );

    // Economy - Coins
    public static final DeferredItem<net.nextfur.fwc.economy.items.CurrencyItem> COIN_1C = ITEMS.register(
            "coin_1c",
            () -> new net.nextfur.fwc.economy.items.CurrencyItem(net.nextfur.fwc.economy.currency.CurrencyUnit.COIN_1C, new Item.Properties())
    );

    public static final DeferredItem<net.nextfur.fwc.economy.items.CurrencyItem> COIN_5C = ITEMS.register(
            "coin_5c",
            () -> new net.nextfur.fwc.economy.items.CurrencyItem(net.nextfur.fwc.economy.currency.CurrencyUnit.COIN_5C, new Item.Properties())
    );

    public static final DeferredItem<net.nextfur.fwc.economy.items.CurrencyItem> COIN_20C = ITEMS.register(
            "coin_20c",
            () -> new net.nextfur.fwc.economy.items.CurrencyItem(net.nextfur.fwc.economy.currency.CurrencyUnit.COIN_20C, new Item.Properties())
    );

    public static final DeferredItem<net.nextfur.fwc.economy.items.CurrencyItem> COIN_25C = ITEMS.register(
            "coin_25c",
            () -> new net.nextfur.fwc.economy.items.CurrencyItem(net.nextfur.fwc.economy.currency.CurrencyUnit.COIN_25C, new Item.Properties())
    );

    public static final DeferredItem<net.nextfur.fwc.economy.items.CurrencyItem> COIN_50C = ITEMS.register(
            "coin_50c",
            () -> new net.nextfur.fwc.economy.items.CurrencyItem(net.nextfur.fwc.economy.currency.CurrencyUnit.COIN_50C, new Item.Properties())
    );

    public static final DeferredItem<net.nextfur.fwc.economy.items.CurrencyItem> COIN_1M = ITEMS.register(
            "coin_1m",
            () -> new net.nextfur.fwc.economy.items.CurrencyItem(net.nextfur.fwc.economy.currency.CurrencyUnit.COIN_1M, new Item.Properties())
    );

    public static final DeferredItem<net.nextfur.fwc.economy.items.CurrencyItem> COIN_2M = ITEMS.register(
            "coin_2m",
            () -> new net.nextfur.fwc.economy.items.CurrencyItem(net.nextfur.fwc.economy.currency.CurrencyUnit.COIN_2M, new Item.Properties())
    );

    // Economy - Bills
    public static final DeferredItem<net.nextfur.fwc.economy.items.CurrencyItem> BILL_5M = ITEMS.register(
            "bill_5m",
            () -> new net.nextfur.fwc.economy.items.CurrencyItem(net.nextfur.fwc.economy.currency.CurrencyUnit.BILL_5M, new Item.Properties())
    );

    public static final DeferredItem<net.nextfur.fwc.economy.items.CurrencyItem> BILL_10M = ITEMS.register(
            "bill_10m",
            () -> new net.nextfur.fwc.economy.items.CurrencyItem(net.nextfur.fwc.economy.currency.CurrencyUnit.BILL_10M, new Item.Properties())
    );

    public static final DeferredItem<net.nextfur.fwc.economy.items.CurrencyItem> BILL_20M = ITEMS.register(
            "bill_20m",
            () -> new net.nextfur.fwc.economy.items.CurrencyItem(net.nextfur.fwc.economy.currency.CurrencyUnit.BILL_20M, new Item.Properties())
    );

    public static final DeferredItem<net.nextfur.fwc.economy.items.CurrencyItem> BILL_50M = ITEMS.register(
            "bill_50m",
            () -> new net.nextfur.fwc.economy.items.CurrencyItem(net.nextfur.fwc.economy.currency.CurrencyUnit.BILL_50M, new Item.Properties())
    );

    public static final DeferredItem<net.nextfur.fwc.economy.items.CurrencyItem> BILL_100M = ITEMS.register(
            "bill_100m",
            () -> new net.nextfur.fwc.economy.items.CurrencyItem(net.nextfur.fwc.economy.currency.CurrencyUnit.BILL_100M, new Item.Properties())
    );

    public static final DeferredItem<net.nextfur.fwc.economy.items.CurrencyItem> BILL_200M = ITEMS.register(
            "bill_200m",
            () -> new net.nextfur.fwc.economy.items.CurrencyItem(net.nextfur.fwc.economy.currency.CurrencyUnit.BILL_200M, new Item.Properties())
    );

    static {
        FwModBlocks.BLOCKS.getEntries().forEach(block -> {
            String blockName = block.getId().getPath();
            if (blockName.contains("plushie")) {
                ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()) {
                    @Override
                    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                        tooltipComponents.add(Component.translatable("block.fursmp." + blockName + ".description").withStyle(ChatFormatting.GRAY));
                        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                    }
                });
            } else {
                ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
            }
        });
    }
}