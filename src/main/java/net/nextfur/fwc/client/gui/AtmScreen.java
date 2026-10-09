package net.nextfur.fwc.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.economy.data.EconomyFormatHelper;
import net.nextfur.fwc.economy.menu.AtmMenu;
import net.nextfur.fwc.network.economy.AtmActionC2SPacket;
import net.nextfur.fwc.pda.data.PdaData;
import net.nextfur.fwc.pda.items.PdaItem;

import java.util.OptionalLong;

public class AtmScreen extends AbstractContainerScreen<AtmMenu> {
    private EditBox customWithdrawBox;
    private Long cachedBalance = null;

    public AtmScreen(AtmMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = 129;
        this.titleLabelY = 6;
    }

    public void onSyncReceived(long newBalance) {
        this.cachedBalance = newBalance;
    }

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos;
        int y = this.topPos;

        // Button: Depositar (Deposits item in slot 1)
        this.addRenderableWidget(Button.builder(Component.literal("Depositar"), b -> {
            PacketDistributor.sendToServer(new AtmActionC2SPacket(AtmActionC2SPacket.ACTION_DEPOSIT_SLOT, 0L));
        }).pos(x + 50, y + 84).size(56, 18).build());

        // Button: Dep. Tudo (Scans inventory and deposits all cash)
        this.addRenderableWidget(Button.builder(Component.literal("Dep. Tudo"), b -> {
            PacketDistributor.sendToServer(new AtmActionC2SPacket(AtmActionC2SPacket.ACTION_DEPOSIT_ALL, 0L));
        }).pos(x + 110, y + 84).size(58, 18).build());

        // Quick Withdraw presets: 10$M, 50$M, 100$M, 500$M
        this.addRenderableWidget(Button.builder(Component.literal("10$M"), b -> {
            PacketDistributor.sendToServer(new AtmActionC2SPacket(AtmActionC2SPacket.ACTION_WITHDRAW, 1000L));
        }).pos(x + 50, y + 104).size(28, 14).build());

        this.addRenderableWidget(Button.builder(Component.literal("50$M"), b -> {
            PacketDistributor.sendToServer(new AtmActionC2SPacket(AtmActionC2SPacket.ACTION_WITHDRAW, 5000L));
        }).pos(x + 80, y + 104).size(28, 14).build());

        this.addRenderableWidget(Button.builder(Component.literal("100$M"), b -> {
            PacketDistributor.sendToServer(new AtmActionC2SPacket(AtmActionC2SPacket.ACTION_WITHDRAW, 10000L));
        }).pos(x + 110, y + 104).size(30, 14).build());

        this.addRenderableWidget(Button.builder(Component.literal("500$M"), b -> {
            PacketDistributor.sendToServer(new AtmActionC2SPacket(AtmActionC2SPacket.ACTION_WITHDRAW, 50000L));
        }).pos(x + 142, y + 104).size(28, 14).build());

        // Custom Withdraw EditBox & Button
        this.customWithdrawBox = new EditBox(this.font, x + 50, y + 120, 70, 14, Component.literal("Valor Saque"));
        this.customWithdrawBox.setHint(Component.literal("Quantia $M"));
        this.addRenderableWidget(this.customWithdrawBox);

        this.addRenderableWidget(Button.builder(Component.literal("Sacar"), b -> {
            OptionalLong parsed = EconomyFormatHelper.parseToCents(this.customWithdrawBox.getValue());
            if (parsed.isPresent() && parsed.getAsLong() > 0) {
                PacketDistributor.sendToServer(new AtmActionC2SPacket(AtmActionC2SPacket.ACTION_WITHDRAW, parsed.getAsLong()));
                this.customWithdrawBox.setValue("");
            }
        }).pos(x + 124, y + 119).size(44, 16).build());
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Container chassis (dark tech panel)
        gui.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFC6C6C6);
        // Bevel highlight
        gui.fill(x, y, x + this.imageWidth - 1, y + 1, 0xFFFFFFFF);
        gui.fill(x, y, x + 1, y + this.imageHeight - 1, 0xFFFFFFFF);
        gui.fill(x + 1, y + 1, x + this.imageWidth - 2, y + 2, 0xFFFFFFFF);
        gui.fill(x + 1, y + 1, x + 2, y + this.imageHeight - 2, 0xFFFFFFFF);
        // Bevel shadow
        gui.fill(x + 1, y + this.imageHeight - 2, x + this.imageWidth, y + this.imageHeight - 1, 0xFF555555);
        gui.fill(x + this.imageWidth - 2, y + 1, x + this.imageWidth - 1, y + this.imageHeight, 0xFF555555);
        gui.fill(x, y + this.imageHeight - 1, x + this.imageWidth, y + this.imageHeight, 0xFF373737);
        gui.fill(x + this.imageWidth - 1, y, x + this.imageWidth, y + this.imageHeight, 0xFF373737);

        // Terminal Digital Display Screen Box (cyber navy screen)
        int screenBoxX = x + 48;
        int screenBoxY = y + 20;
        int screenBoxW = 120;
        int screenBoxH = 60;

        gui.fill(screenBoxX, screenBoxY, screenBoxX + screenBoxW, screenBoxY + screenBoxH, 0xFF0A131F);
        gui.fill(screenBoxX + 1, screenBoxY + 1, screenBoxX + screenBoxW - 1, screenBoxY + screenBoxH - 1, 0xFF0F1B2C);
        gui.renderOutline(screenBoxX, screenBoxY, screenBoxW, screenBoxH, 0xFF00FFB2);

        // Slot 0 (PDA Reader Slot) background & frame
        int pdaSlotX = x + 23;
        int pdaSlotY = y + 46;
        gui.fill(pdaSlotX, pdaSlotY, pdaSlotX + 18, pdaSlotY + 18, 0xFF142434);
        gui.renderOutline(pdaSlotX - 1, pdaSlotY - 1, 20, 20, 0xFF00C0F0);

        // Slot 1 (Cash / Deposit Slot) background & frame
        int cashSlotX = x + 23;
        int cashSlotY = y + 86;
        gui.fill(cashSlotX, cashSlotY, cashSlotX + 18, cashSlotY + 18, 0xFF2A2E20);
        gui.renderOutline(cashSlotX - 1, cashSlotY - 1, 20, 20, 0xFF55FF55);

        // Inventory slots background bevels
        for (Slot slot : this.menu.slots) {
            if (slot.index >= 2) {
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
        // ATM Header
        gui.drawString(this.font, ChatFormatting.BOLD + "ATM - Shield Bank", 8, 6, 0x202020, false);

        // Terminal Screen Text Display
        ItemStack pdaStack = this.menu.getPdaStack();
        boolean hasPda = !pdaStack.isEmpty() && pdaStack.getItem() instanceof PdaItem;

        if (hasPda) {
            PdaData pdaData = this.menu.getPdaData();
            long balance = (cachedBalance != null) ? cachedBalance : (pdaData != null ? pdaData.bankBalanceCents() : 0L);
            String owner = (pdaData != null && pdaData.hasOwner()) ? pdaData.ownerName() : "Não vinculado";

            gui.drawString(this.font, "§a● PDA CONECTADO", 52, 23, 0xFFFFFF, false);
            gui.drawString(this.font, "§7Titular: §f" + owner, 52, 34, 0xFFFFFF, false);
            gui.drawString(this.font, "§bSaldo Shield Bank:", 52, 47, 0xFFFFFF, false);
            gui.drawString(this.font, "§e§l" + EconomyFormatHelper.formatStandard(balance), 52, 59, 0xFFFFFF, false);
            gui.drawString(this.font, "§8(" + EconomyFormatHelper.formatDenomination(balance) + ")", 52, 69, 0xFFFFFF, false);
        } else {
            cachedBalance = null;
            gui.drawString(this.font, "§cAGUARDANDO PDA...", 52, 24, 0xFFFFFF, false);
            gui.drawString(this.font, "§7Insira seu PDA ;3", 52, 38, 0xFFFFFF, false);
        }

        // Inventory label
        gui.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
    }
}
