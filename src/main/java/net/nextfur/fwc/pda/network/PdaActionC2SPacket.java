package net.nextfur.fwc.pda.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.init.FwDataComponents;
import net.nextfur.fwc.pda.data.PdaData;
import net.nextfur.fwc.pda.data.PdaMessage;
import net.nextfur.fwc.pda.data.PdaNote;
import net.nextfur.fwc.pda.items.PdaItem;

import java.util.Optional;
import java.util.UUID;

public record PdaActionC2SPacket(
        int action,
        boolean isMainHand,
        Optional<UUID> targetUuid,
        String payload1,
        String payload2,
        Optional<UUID> noteId
) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "pda_action");
    public static final Type<PdaActionC2SPacket> TYPE = new Type<>(ID);

    public static final int ACTION_SEND_MESSAGE = 1;
    public static final int ACTION_SAVE_NOTE = 2;
    public static final int ACTION_DELETE_NOTE = 3;
    public static final int ACTION_RESET_PDA = 4;
    public static final int ACTION_DELETE_CONTACT = 5;

    public static final StreamCodec<ByteBuf, PdaActionC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                ByteBufCodecs.VAR_INT.encode(buf, pkt.action);
                ByteBufCodecs.BOOL.encode(buf, pkt.isMainHand);
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).encode(buf, pkt.targetUuid);
                ByteBufCodecs.STRING_UTF8.encode(buf, pkt.payload1);
                ByteBufCodecs.STRING_UTF8.encode(buf, pkt.payload2);
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).encode(buf, pkt.noteId);
            },
            buf -> new PdaActionC2SPacket(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PdaActionC2SPacket packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;

        ctx.enqueueWork(() -> {
            ItemStack stack = player.getItemInHand(packet.isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            if (!(stack.getItem() instanceof PdaItem)) {
                return;
            }

            PdaData data = stack.get(FwDataComponents.PDA_DATA.get());
            if (data == null) {
                return;
            }

            switch (packet.action) {
                case ACTION_SEND_MESSAGE -> {
                    if (packet.targetUuid.isEmpty() || packet.payload1.isBlank()) return;
                    UUID targetUuid = packet.targetUuid.get();
                    String messageText = packet.payload1.trim();

                    String senderName = data.hasOwner() ? data.ownerName() : player.getName().getString();
                    UUID senderUuid = data.hasOwner() ? data.ownerUuid().orElse(player.getUUID()) : player.getUUID();

                    ServerPlayer recipient = player.server.getPlayerList().getPlayer(targetUuid);
                    if (recipient != null) {
                        Component recipientMsg = Component.empty()
                                .append(Component.literal("§3[§b📱 FurWatch PDA §8// §eDe: §b" + senderName + "§3] §f" + messageText));
                        recipient.sendSystemMessage(recipientMsg);
                        recipient.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0f, 1.8f);

                        Component senderMsg = Component.empty()
                                .append(Component.literal("§3[§b📱 FurWatch PDA §8// §7Para: §b" + recipient.getName().getString() + "§3] §f" + messageText));
                        player.sendSystemMessage(senderMsg);

                        PdaMessage msgRecord = new PdaMessage(
                                UUID.randomUUID(),
                                senderUuid,
                                senderName,
                                recipient.getUUID(),
                                recipient.getName().getString(),
                                messageText,
                                System.currentTimeMillis()
                        );
                        PdaData updated = data.withMessage(msgRecord);
                        stack.set(FwDataComponents.PDA_DATA.get(), updated);
                        PacketDistributor.sendToPlayer(player, new SyncPdaDataS2CPacket(updated));

                        for (ItemStack rStack : recipient.getInventory().items) {
                            if (rStack.getItem() instanceof PdaItem) {
                                PdaData rData = rStack.get(FwDataComponents.PDA_DATA.get());
                                if (rData != null) {
                                    rStack.set(FwDataComponents.PDA_DATA.get(), rData.withMessage(msgRecord));
                                    break;
                                }
                            }
                        }
                    } else {
                        player.sendSystemMessage(Component.literal("§8[§b📱 FurWatch PDA§8] §cO contato está offline no momento."));
                    }
                }
                case ACTION_SAVE_NOTE -> {
                    UUID noteId = packet.noteId.orElse(UUID.randomUUID());
                    String title = packet.payload1.isBlank() ? "Nota sem título" : packet.payload1.trim();
                    String content = packet.payload2;
                    PdaNote note = new PdaNote(noteId, title, content, System.currentTimeMillis());
                    PdaData updated = data.withNote(note);
                    stack.set(FwDataComponents.PDA_DATA.get(), updated);
                    PacketDistributor.sendToPlayer(player, new SyncPdaDataS2CPacket(updated));
                }
                case ACTION_DELETE_NOTE -> {
                    if (packet.noteId.isPresent()) {
                        PdaData updated = data.withoutNote(packet.noteId.get());
                        stack.set(FwDataComponents.PDA_DATA.get(), updated);
                        PacketDistributor.sendToPlayer(player, new SyncPdaDataS2CPacket(updated));
                    }
                }
                case ACTION_RESET_PDA -> {
                    PdaData updated = data.resetOwner();
                    stack.set(FwDataComponents.PDA_DATA.get(), updated);
                    player.sendSystemMessage(Component.literal("§8[§b📱 FurWatch PDA§8] §eDispositivo redefinido com sucesso! O proprietário foi desvinculado."));
                    PacketDistributor.sendToPlayer(player, new SyncPdaDataS2CPacket(updated));
                }
                case ACTION_DELETE_CONTACT -> {
                    if (packet.targetUuid.isPresent()) {
                        PdaData updated = data.withoutContact(packet.targetUuid.get());
                        stack.set(FwDataComponents.PDA_DATA.get(), updated);
                        PacketDistributor.sendToPlayer(player, new SyncPdaDataS2CPacket(updated));
                    }
                }
            }
        });
    }
}
