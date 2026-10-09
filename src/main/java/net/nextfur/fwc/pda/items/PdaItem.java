package net.nextfur.fwc.pda.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.init.FwDataComponents;
import net.nextfur.fwc.pda.data.PdaColor;
import net.nextfur.fwc.pda.data.PdaContact;
import net.nextfur.fwc.pda.data.PdaData;
import net.nextfur.fwc.pda.network.OpenPdaS2CPacket;

import java.util.List;
import java.util.UUID;

public class PdaItem extends Item {
    private final PdaColor colorVariant;

    public PdaItem(PdaColor colorVariant, Properties properties) {
        super(properties.stacksTo(1));
        this.colorVariant = colorVariant;
    }

    public PdaColor getColorVariant() {
        return colorVariant;
    }

    public PdaData getOrCreatePdaData(ItemStack stack, Player player) {
        PdaData data = stack.get(FwDataComponents.PDA_DATA.get());
        if (data == null) {
            data = PdaData.createNew(this.colorVariant);
            if (player != null && !player.level().isClientSide) {
                data = data.withOwner(player.getUUID(), player.getName().getString());
            }
            stack.set(FwDataComponents.PDA_DATA.get(), data);
        } else if (!data.hasOwner() && player != null && !player.level().isClientSide) {
            data = data.withOwner(player.getUUID(), player.getName().getString());
            stack.set(FwDataComponents.PDA_DATA.get(), data);
        }
        return data;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            getOrCreatePdaData(stack, serverPlayer);
            PacketDistributor.sendToPlayer(serverPlayer, new OpenPdaS2CPacket(stack, hand == InteractionHand.MAIN_HAND));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.getBlockState(context.getClickedPos()).getBlock() instanceof net.nextfur.fwc.economy.blocks.AbstractAtmBlock) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player != null && !player.isShiftKeyDown()) {
            ItemStack stack = context.getItemInHand();
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                getOrCreatePdaData(stack, serverPlayer);
                PacketDistributor.sendToPlayer(serverPlayer, new OpenPdaS2CPacket(stack, context.getHand() == InteractionHand.MAIN_HAND));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand hand) {
        if (interactionTarget instanceof Player targetPlayer) {
            if (!player.level().isClientSide) {
                PdaData data = getOrCreatePdaData(stack, player);
                UUID targetUuid = targetPlayer.getUUID();
                String targetName = targetPlayer.getName().getString();

                if (data.hasContact(targetUuid)) {
                    player.displayClientMessage(
                            Component.literal("§8[§b📱 FurWatch PDA§8] §e" + targetName + " §7já está salvo na sua lista de contatos.")
                                    .withStyle(ChatFormatting.YELLOW),
                            true
                    );
                } else {
                    PdaData updated = data.withContact(new PdaContact(targetUuid, targetName));
                    stack.set(FwDataComponents.PDA_DATA.get(), updated);
                    player.displayClientMessage(
                            Component.literal("§8[§b📱 FurWatch PDA§8] §aContato salvo: §f" + targetName + " §acom sucesso!")
                                    .withStyle(ChatFormatting.GREEN),
                            true
                    );
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 1.0f, 1.6f);
                    }
                }
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide());
        }
        return super.interactLivingEntity(stack, player, interactionTarget, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        PdaData data = stack.get(FwDataComponents.PDA_DATA.get());

        if (data != null && data.hasOwner()) {
            tooltipComponents.add(Component.literal("Proprietário: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(data.ownerName()).withStyle(ChatFormatting.WHITE)));
            tooltipComponents.add(Component.literal("Shield Bank: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(net.nextfur.fwc.economy.data.EconomyFormatHelper.formatStandard(data.bankBalanceCents())).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
            tooltipComponents.add(Component.literal("Contatos: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.valueOf(data.contacts().size())).withStyle(ChatFormatting.DARK_AQUA)));
            tooltipComponents.add(Component.literal("Notas: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.valueOf(data.notes().size())).withStyle(ChatFormatting.DARK_AQUA)));
            tooltipComponents.add(Component.literal("ID: ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("#" + data.pdaId().toString().substring(0, 8)).withStyle(ChatFormatting.DARK_GRAY)));
        } else {
            tooltipComponents.add(Component.literal("Proprietário: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Não vinculado").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
            tooltipComponents.add(Component.literal("Use para vincular automaticamente.").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
