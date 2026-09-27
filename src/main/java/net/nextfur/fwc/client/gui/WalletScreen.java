package net.nextfur.fwc.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.economy.currency.CurrencyUnit;
import net.nextfur.fwc.economy.data.EconomyFormatHelper;
import net.nextfur.fwc.economy.data.WalletData;
import net.nextfur.fwc.economy.menu.WalletMenu;
import net.nextfur.fwc.network.economy.WalletActionC2SPacket;

import java.util.OptionalLong;

public class WalletScreen extends AbstractContainerScreen<WalletMenu> {
    private WalletData walletData;
    private EditBox checkAmountBox;
    private EditBox checkPayeeBox;
    private EditBox customWithdrawBox;

    public WalletScreen(WalletMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 236;
        this.inventoryLabelY = 142;
        this.titleLabelY = 7;
        this.walletData = menu.getWalletData();
    }

    public void updateWalletData(WalletData data) {
        this.walletData = data;
    }

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos;
        int y = this.topPos;

        // Button: Depositar (Deposits item in slot 0)
        this.addRenderableWidget(Button.builder(Component.literal("Depositar"), b -> {
            PacketDistributor.sendToServer(new WalletActionC2SPacket(WalletActionC2SPacket.ACTION_DEPOSIT_SLOT, 0L, ""));
        }).pos(x + 42, y + 34).size(56, 18).build());

        // Button: Dep. Tudo (Scans inventory and deposits all cash)
        this.addRenderableWidget(Button.builder(Component.literal("Dep. Tudo"), b -> {
            PacketDistributor.sendToServer(new WalletActionC2SPacket(WalletActionC2SPacket.ACTION_DEPOSIT_ALL, 0L, ""));
        }).pos(x + 102, y + 34).size(58, 18).build());

        // Quick Withdraw: Coins row (7 coins) - Each rendered as an interactive item icon slot!
        CurrencyUnit[] coins = {
                CurrencyUnit.COIN_1C, CurrencyUnit.COIN_5C, CurrencyUnit.COIN_20C,
                CurrencyUnit.COIN_25C, CurrencyUnit.COIN_50C, CurrencyUnit.COIN_1M, CurrencyUnit.COIN_2M
        };
        for (int i = 0; i < coins.length; i++) {
            this.addRenderableWidget(new CurrencyIconWidget(x + 15 + i * 21, y + 66, coins[i]));
        }

        // Quick Withdraw: Bills row (6 bills) - Each rendered as an interactive item icon slot!
        CurrencyUnit[] bills = {
                CurrencyUnit.BILL_5M, CurrencyUnit.BILL_10M, CurrencyUnit.BILL_20M,
                CurrencyUnit.BILL_50M, CurrencyUnit.BILL_100M, CurrencyUnit.BILL_200M
        };
        for (int i = 0; i < bills.length; i++) {
            this.addRenderableWidget(new CurrencyIconWidget(x + 25 + i * 21, y + 87, bills[i]));
        }

        // Custom Withdraw Section (Left)
        this.customWithdrawBox = new EditBox(this.font, x + 8, y + 116, 44, 14, Component.literal("Valor Saque"));
        this.customWithdrawBox.setHint(Component.literal("$M"));
        this.addRenderableWidget(this.customWithdrawBox);

        this.addRenderableWidget(Button.builder(Component.literal("Sacar"), b -> {
            OptionalLong parsed = EconomyFormatHelper.parseToCents(this.customWithdrawBox.getValue());
            if (parsed.isPresent() && parsed.getAsLong() > 0) {
                PacketDistributor.sendToServer(new WalletActionC2SPacket(WalletActionC2SPacket.ACTION_WITHDRAW_CENTS, parsed.getAsLong(), ""));
                this.customWithdrawBox.setValue("");
            }
        }).pos(x + 54, y + 115).size(36, 16).build());

        // Check Section (Right)
        this.checkAmountBox = new EditBox(this.font, x + 98, y + 116, 36, 14, Component.literal("Valor Cheque"));
        this.checkAmountBox.setHint(Component.literal("$M"));
        this.addRenderableWidget(this.checkAmountBox);

        this.addRenderableWidget(Button.builder(Component.literal("Emitir"), b -> {
            OptionalLong parsed = EconomyFormatHelper.parseToCents(this.checkAmountBox.getValue());
            if (parsed.isPresent() && parsed.getAsLong() > 0) {
                String payee = this.checkPayeeBox.getValue().trim();
                PacketDistributor.sendToServer(new WalletActionC2SPacket(WalletActionC2SPacket.ACTION_WRITE_CHECK, parsed.getAsLong(), payee));
                this.checkAmountBox.setValue("");
                this.checkPayeeBox.setValue("Portador");
            }
        }).pos(x + 136, y + 115).size(32, 16).build());

        this.checkPayeeBox = new EditBox(this.font, x + 98, y + 132, 70, 12, Component.literal("Favorecido"));
        this.checkPayeeBox.setValue("Portador");
        this.addRenderableWidget(this.checkPayeeBox);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Container frame (standard Minecraft beige/gray panel)
        gui.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFC6C6C6);
        // Bevel highlight (top & left)
        gui.fill(x, y, x + this.imageWidth - 1, y + 1, 0xFFFFFFFF);
        gui.fill(x, y, x + 1, y + this.imageHeight - 1, 0xFFFFFFFF);
        gui.fill(x + 1, y + 1, x + this.imageWidth - 2, y + 2, 0xFFFFFFFF);
        gui.fill(x + 1, y + 1, x + 2, y + this.imageHeight - 2, 0xFFFFFFFF);
        // Bevel shadow (bottom & right)
        gui.fill(x + 1, y + this.imageHeight - 2, x + this.imageWidth, y + this.imageHeight - 1, 0xFF555555);
        gui.fill(x + this.imageWidth - 2, y + 1, x + this.imageWidth - 1, y + this.imageHeight, 0xFF555555);
        gui.fill(x, y + this.imageHeight - 1, x + this.imageWidth, y + this.imageHeight, 0xFF373737);
        gui.fill(x + this.imageWidth - 1, y, x + this.imageWidth, y + this.imageHeight, 0xFF373737);

        // Header dark plate (for crisp title and balance display)
        gui.fill(x + 6, y + 6, x + this.imageWidth - 6, y + 29, 0xFF222222);
        gui.fill(x + 6, y + 6, x + this.imageWidth - 6, y + 7, 0xFF141414);
        gui.fill(x + 6, y + 6, x + 7, y + 29, 0xFF141414);
        gui.fill(x + 6, y + 28, x + this.imageWidth - 6, y + 29, 0xFF3A3A3A);
        gui.fill(x + this.imageWidth - 7, y + 6, x + this.imageWidth - 6, y + 29, 0xFF3A3A3A);

        // Deposit slot background (vanilla 3D sunken bevel)
        gui.fill(x + 17, y + 34, x + 35, y + 35, 0xFF373737);
        gui.fill(x + 17, y + 34, x + 18, y + 52, 0xFF373737);
        gui.fill(x + 18, y + 51, x + 35, y + 52, 0xFFFFFFFF);
        gui.fill(x + 34, y + 35, x + 35, y + 52, 0xFFFFFFFF);
        gui.fill(x + 18, y + 35, x + 34, y + 51, 0xFF8B8B8B);

        // Section divider
        gui.fill(x + 8, y + 107, x + this.imageWidth - 8, y + 108, 0xFF8B8B8B);
        gui.fill(x + 8, y + 108, x + this.imageWidth - 8, y + 109, 0xFFFFFFFF);

        // Player Inventory slots background (vanilla 3D sunken bevels)
        for (Slot slot : this.menu.slots) {
            if (slot.index > 0) {
                int sx = x + slot.x - 1;
                int sy = y + slot.y - 1;
                gui.fill(sx, sy, sx + 18, sy + 1, 0xFF373737);
                gui.fill(sx, sy, sx + 1, sy + 18, 0xFF373737);
                gui.fill(sx + 1, sy + 17, sx + 18, sy + 18, 0xFFFFFFFF);
                gui.fill(sx + 17, sy + 1, sx + 18, sy + 18, 0xFFFFFFFF);
                gui.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        String owner = (walletData != null) ? walletData.ownerName() : "Desconhecido";
        long balance = (walletData != null) ? walletData.balanceCents() : 0L;

        // Header Title
        gui.drawString(this.font, ChatFormatting.GOLD + "Shield Wallet" + ChatFormatting.GRAY + " (" + owner + ")", 10, 8, 0xFFFFFF, false);
        gui.drawString(this.font, ChatFormatting.WHITE + "Saldo: " + ChatFormatting.GREEN + ChatFormatting.BOLD + EconomyFormatHelper.formatStandard(balance) +
                ChatFormatting.DARK_GRAY + " (" + EconomyFormatHelper.formatDenomination(balance) + ")", 10, 18, 0xFFFFFF, false);

        // Quick withdraw label
        gui.drawString(this.font, ChatFormatting.DARK_GRAY + "Saque Rápido:", 8, 55, 0x404040, false);

        // Custom withdraw & Check labels
        gui.drawString(this.font, ChatFormatting.DARK_GRAY + "Sacar:", 8, 107, 0x404040, false);
        gui.drawString(this.font, ChatFormatting.DARK_GRAY + "Cheque:", 98, 107, 0x404040, false);

        // Inventory label
        gui.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
    }
}
