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
        this.imageHeight = 222;
        this.inventoryLabelY = 132;
        this.titleLabelY = 6;
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

        // Button: Depositar item do slot
        this.addRenderableWidget(Button.builder(Component.literal("Depositar"), b -> {
            PacketDistributor.sendToServer(new WalletActionC2SPacket(WalletActionC2SPacket.ACTION_DEPOSIT_SLOT, 0L, ""));
        }).pos(x + 44, y + 41).size(60, 18).build());

        // Button: Depositar Tudo
        this.addRenderableWidget(Button.builder(Component.literal("Dep. Tudo"), b -> {
            PacketDistributor.sendToServer(new WalletActionC2SPacket(WalletActionC2SPacket.ACTION_DEPOSIT_ALL, 0L, ""));
        }).pos(x + 106, y + 41).size(64, 18).build());

        // Quick Withdraw buttons row 1
        CurrencyUnit[] row1 = {CurrencyUnit.COIN_1C, CurrencyUnit.COIN_5C, CurrencyUnit.COIN_20C, CurrencyUnit.COIN_50C, CurrencyUnit.COIN_1M, CurrencyUnit.COIN_2M};
        int btnW = 26;
        int btnH = 15;
        for (int i = 0; i < row1.length; i++) {
            CurrencyUnit u = row1[i];
            this.addRenderableWidget(Button.builder(Component.literal(u.getLabel()), b -> {
                PacketDistributor.sendToServer(new WalletActionC2SPacket(WalletActionC2SPacket.ACTION_WITHDRAW_CENTS, u.getValueInCents(), ""));
            }).pos(x + 7 + i * (btnW + 2), y + 68).size(btnW, btnH).build());
        }

        // Quick Withdraw buttons row 2
        CurrencyUnit[] row2 = {CurrencyUnit.BILL_5M, CurrencyUnit.BILL_10M, CurrencyUnit.BILL_20M, CurrencyUnit.BILL_50M, CurrencyUnit.BILL_100M, CurrencyUnit.BILL_200M};
        for (int i = 0; i < row2.length; i++) {
            CurrencyUnit u = row2[i];
            this.addRenderableWidget(Button.builder(Component.literal(u.getLabel()), b -> {
                PacketDistributor.sendToServer(new WalletActionC2SPacket(WalletActionC2SPacket.ACTION_WITHDRAW_CENTS, u.getValueInCents(), ""));
            }).pos(x + 7 + i * (btnW + 2), y + 85).size(btnW, btnH).build());
        }

        // Custom withdraw box & button
        this.customWithdrawBox = new EditBox(this.font, x + 7, y + 104, 48, 14, Component.literal("Valor Saque"));
        this.customWithdrawBox.setHint(Component.literal("Ex: 15.50"));
        this.addRenderableWidget(this.customWithdrawBox);

        this.addRenderableWidget(Button.builder(Component.literal("Sacar"), b -> {
            OptionalLong parsed = EconomyFormatHelper.parseToCents(this.customWithdrawBox.getValue());
            if (parsed.isPresent() && parsed.getAsLong() > 0) {
                PacketDistributor.sendToServer(new WalletActionC2SPacket(WalletActionC2SPacket.ACTION_WITHDRAW_CENTS, parsed.getAsLong(), ""));
                this.customWithdrawBox.setValue("");
            }
        }).pos(x + 58, y + 103).size(40, 16).build());

        // Write Check: Amount Box, Payee Box, Emit Button
        this.checkAmountBox = new EditBox(this.font, x + 102, y + 104, 34, 14, Component.literal("Valor Cheque"));
        this.checkAmountBox.setHint(Component.literal("$M"));
        this.addRenderableWidget(this.checkAmountBox);

        this.checkPayeeBox = new EditBox(this.font, x + 7, y + 121, 91, 14, Component.literal("Favorecido"));
        this.checkPayeeBox.setValue("Portador");
        this.addRenderableWidget(this.checkPayeeBox);

        this.addRenderableWidget(Button.builder(Component.literal("Emitir"), b -> {
            OptionalLong parsed = EconomyFormatHelper.parseToCents(this.checkAmountBox.getValue());
            if (parsed.isPresent() && parsed.getAsLong() > 0) {
                String payee = this.checkPayeeBox.getValue().trim();
                PacketDistributor.sendToServer(new WalletActionC2SPacket(WalletActionC2SPacket.ACTION_WRITE_CHECK, parsed.getAsLong(), payee));
                this.checkAmountBox.setValue("");
                this.checkPayeeBox.setValue("Portador");
            }
        }).pos(x + 138, y + 103).size(32, 16).build());
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Background panel
        gui.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFC6C6C6);
        gui.fill(x, y, x + this.imageWidth, y + 2, 0xFFFFFFFF);
        gui.fill(x, y, x + 2, y + this.imageHeight, 0xFFFFFFFF);
        gui.fill(x + this.imageWidth - 2, y, x + this.imageWidth, y + this.imageHeight, 0xFF555555);
        gui.fill(x, y + this.imageHeight - 2, x + this.imageWidth, y + this.imageHeight, 0xFF555555);

        // Header banner (dark panel)
        gui.fill(x + 5, y + 5, x + this.imageWidth - 5, y + 36, 0xFF222222);

        // Deposit slot background
        gui.fill(x + 23, y + 41, x + 41, y + 59, 0xFF373737);
        gui.fill(x + 24, y + 42, x + 40, y + 58, 0xFF8B8B8B);

        // Inventory slots background
        for (Slot slot : this.menu.slots) {
            if (slot.index > 0) {
                int sx = x + slot.x - 1;
                int sy = y + slot.y - 1;
                gui.fill(sx, sy, sx + 18, sy + 18, 0xFF373737);
                gui.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        // Draw Header text
        String owner = (walletData != null) ? walletData.ownerName() : "Desconhecido";
        long balance = (walletData != null) ? walletData.balanceCents() : 0L;

        gui.drawString(this.font, ChatFormatting.GOLD + "Carteira FurSMP" + ChatFormatting.GRAY + " (" + owner + ")", 10, 8, 0xFFFFFF, false);
        gui.drawString(this.font, ChatFormatting.WHITE + "Saldo: " + ChatFormatting.GREEN + ChatFormatting.BOLD + EconomyFormatHelper.formatStandard(balance) +
                ChatFormatting.DARK_GRAY + " (" + EconomyFormatHelper.formatDenomination(balance) + ")", 10, 20, 0xFFFFFF, false);

        // Quick withdraw label
        gui.drawString(this.font, ChatFormatting.DARK_GRAY + "Saque Rápido:", 8, 59, 0x404040, false);

        // Custom withdraw / Cheque label
        gui.drawString(this.font, ChatFormatting.DARK_GRAY + "Personalizado:", 8, 95, 0x404040, false);
        gui.drawString(this.font, ChatFormatting.DARK_GRAY + "Cheque:", 102, 95, 0x404040, false);

        // Inventory label
        gui.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
    }
}
