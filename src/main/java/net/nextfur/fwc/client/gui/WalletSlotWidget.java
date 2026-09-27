package net.nextfur.fwc.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.client.economy.ClientWalletHolder;
import net.nextfur.fwc.economy.data.EconomyFormatHelper;
import net.nextfur.fwc.economy.data.WalletData;
import net.nextfur.fwc.economy.items.WalletItem;
import net.nextfur.fwc.init.FwDataComponents;
import net.nextfur.fwc.network.economy.EquipWalletSlotC2SPacket;

import java.util.ArrayList;
import java.util.List;

public class WalletSlotWidget extends AbstractWidget {
    public static final ResourceLocation WALLET_ICON = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "textures/item/wallet.png");
    private final InventoryScreen screen;

    public WalletSlotWidget(InventoryScreen screen, int x, int y) {
        super(x, y, 18, 18, Component.literal("Slot de Carteira"));
        this.screen = screen;
    }

    public void updatePosition(int x, int y) {
        setX(x);
        setY(y);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack cursor = screen.getMenu().getCarried();

        if (Screen.hasShiftDown()) {
            // Shift-click slot -> Unequip to inventory
            if (ClientWalletHolder.hasWallet()) {
                PacketDistributor.sendToServer(new EquipWalletSlotC2SPacket(1));
            }
        } else if (cursor.getItem() instanceof WalletItem || cursor.isEmpty()) {
            // Standard slot behavior: pick up wallet or place/swap wallet
            PacketDistributor.sendToServer(new EquipWalletSlotC2SPacket(0));
        }
    }

    @Override
    protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        // Draw standard Minecraft-style slot background
        gui.fill(getX(), getY(), getX() + 18, getY() + 18, 0xFF373737);
        gui.fill(getX() + 1, getY() + 1, getX() + 17, getY() + 17, 0xFF8B8B8B);
        gui.fill(getX() + 1, getY() + 1, getX() + 16, getY() + 16, 0xFF373737);
        gui.fill(getX() + 2, getY() + 2, getX() + 16, getY() + 16, 0xFF8B8B8B);

        ItemStack wallet = ClientWalletHolder.getEquippedWallet();

        if (!wallet.isEmpty()) {
            // Render equipped wallet item
            gui.renderItem(wallet, getX() + 1, getY() + 1);
        } else {
            // Render ghost wallet icon
            RenderSystem.enableBlend();
            gui.setColor(1.0F, 1.0F, 1.0F, 0.40F);
            gui.blit(WALLET_ICON, getX() + 1, getY() + 1, 0, 0, 16, 16, 16, 16);
            gui.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }

        // Slot hover highlight
        if (isHovered()) {
            gui.fill(getX() + 1, getY() + 1, getX() + 17, getY() + 17, 0x80FFFFFF);

            List<Component> tooltip = new ArrayList<>();
            if (!wallet.isEmpty()) {
                WalletData data = wallet.get(FwDataComponents.WALLET_DATA.get());
                tooltip.add(Component.literal("Carteira Equipada").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                if (data != null) {
                    tooltip.add(Component.literal("Titular: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(data.ownerName()).withStyle(ChatFormatting.WHITE)));
                    tooltip.add(Component.literal("Saldo: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(EconomyFormatHelper.formatStandard(data.balanceCents())).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
                }
                tooltip.add(Component.literal("[Clique] Pegar / Trocar carteira").withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.literal("[Shift + Clique] Mover para o inventário").withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.literal("Slot de Carteira").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                tooltip.add(Component.literal("Nenhuma carteira equipada.").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("Coloque uma carteira aqui para equipar.").withStyle(ChatFormatting.YELLOW));
            }
            gui.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
