package net.nextfur.fwc.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.client.economy.ClientWalletHolder;
import net.nextfur.fwc.economy.data.EconomyFormatHelper;
import net.nextfur.fwc.economy.data.WalletData;
import net.nextfur.fwc.init.FwDataComponents;
import net.nextfur.fwc.network.economy.OpenWalletMenuC2SPacket;

import java.util.ArrayList;
import java.util.List;

public class WalletSideTabWidget extends AbstractWidget {
    private final InventoryScreen screen;

    public WalletSideTabWidget(InventoryScreen screen, int x, int y) {
        super(x, y, 28, 28, Component.literal("Menu da Carteira"));
        this.screen = screen;
    }

    public void updatePosition(int x, int y) {
        setX(x);
        setY(y);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!ClientWalletHolder.hasWallet()) return;

        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        PacketDistributor.sendToServer(new OpenWalletMenuC2SPacket());
    }

    @Override
    protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        // ONLY visible when the player has an equipped wallet!
        if (!ClientWalletHolder.hasWallet()) {
            this.visible = false;
            this.active = false;
            return;
        }

        this.visible = true;
        this.active = true;

        int x = getX();
        int y = getY();
        int w = this.width;
        int h = this.height;

        // Draw side tab with vanilla tab styling
        boolean hovered = isHovered();
        int bgColor = hovered ? 0xFFE0E0E0 : 0xFFC6C6C6;

        // Tab background (extending from the left side of the inventory GUI)
        gui.fill(x + 2, y + 2, x + w, y + h - 2, bgColor);
        gui.fill(x, y + 4, x + 2, y + h - 4, bgColor);

        // Tab border
        gui.fill(x + 2, y, x + w, y + 2, 0xFFFFFFFF); // Top highlight
        gui.fill(x, y + 2, x + 2, y + 4, 0xFFFFFFFF);
        gui.fill(x, y + 4, x + 2, y + h - 4, 0xFF373737); // Left shadow
        gui.fill(x, y + h - 4, x + 2, y + h - 2, 0xFF555555);
        gui.fill(x + 2, y + h - 2, x + w, y + h, 0xFF555555); // Bottom shadow

        // Render wallet item inside the tab
        ItemStack wallet = ClientWalletHolder.getEquippedWallet();
        gui.renderItem(wallet, x + 6, y + 6);

        if (hovered) {
            gui.fill(x + 2, y + 2, x + w, y + h - 2, 0x40FFFFFF);

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("Shield Wallet").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

            WalletData data = wallet.get(FwDataComponents.WALLET_DATA.get());
            if (data != null) {
                tooltip.add(Component.literal("Titular: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(data.ownerName()).withStyle(ChatFormatting.WHITE)));
                tooltip.add(Component.literal("Saldo: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(EconomyFormatHelper.formatStandard(data.balanceCents())).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
            }

            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("● Clique para abrir o menu da carteira").withStyle(ChatFormatting.YELLOW));

            gui.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
