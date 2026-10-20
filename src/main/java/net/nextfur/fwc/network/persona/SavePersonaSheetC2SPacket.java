package net.nextfur.fwc.network.persona;

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
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.init.FwPermissions;
import net.nextfur.fwc.persona.PersonaData;
import net.nextfur.fwc.persona.PersonaService;

import java.util.UUID;

public record SavePersonaSheetC2SPacket(
        UUID targetUuid,
        PersonaData data,
        boolean adminEditMode
) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "save_persona_sheet");
    public static final Type<SavePersonaSheetC2SPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, SavePersonaSheetC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                UUIDUtil.STREAM_CODEC.encode(buf, pkt.targetUuid);
                PersonaData.STREAM_CODEC.encode(buf, pkt.data);
                ByteBufCodecs.BOOL.encode(buf, pkt.adminEditMode);
            },
            buf -> new SavePersonaSheetC2SPacket(
                    UUIDUtil.STREAM_CODEC.decode(buf),
                    PersonaData.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SavePersonaSheetC2SPacket packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer sender)) {
            return;
        }

        ctx.enqueueWork(() -> {
            boolean isTargetingOther = !sender.getUUID().equals(packet.targetUuid());
            if (packet.adminEditMode() || isTargetingOther) {
                if (!FwPermissions.hasAdminPermission(sender.createCommandSourceStack())) {
                    sender.sendSystemMessage(Component.literal("§c[FurWatch] Sem permissão para editar a ficha de outros jogadores."));
                    return;
                }

                ServerPlayer target = sender.server.getPlayerList().getPlayer(packet.targetUuid());
                if (target == null) {
                    sender.sendSystemMessage(Component.literal("§c[FurWatch] O jogador alvo não está online."));
                    return;
                }

                PersonaService.setPersonaData(target, packet.data());
                sender.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0f, 1.3f);
                sender.sendSystemMessage(Component.literal("§6[FurWatch] §aFicha de §e" + target.getName().getString() + " §aatualizada!"));

                if (!sender.getUUID().equals(target.getUUID())) {
                    target.sendSystemMessage(Component.literal("§6[FurWatch] §eSua ficha foi atualizada pela Staff."));
                }
                return;
            }

            PersonaData current = PersonaService.getPersonaData(sender);
            if (current.locked()) {
                sender.sendSystemMessage(Component.literal("§c[FurWatch] Sua ficha já foi salva e está bloqueada."));
                return;
            }

            int totalPool = current.getTotalPointsPool();
            int newSpent = packet.data().getSpentPoints();
            int remaining = totalPool - newSpent;

            if (remaining < 0) {
                sender.sendSystemMessage(Component.literal("§c[FurWatch] Pontos insuficientes para salvar esta configuração."));
                return;
            }

            PersonaData saved = packet.data()
                    .withAvailablePoints(remaining)
                    .withLocked(true);

            PersonaService.setPersonaData(sender, saved);
            sender.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.2f);
            sender.sendSystemMessage(Component.literal("§6[FurWatch] §aSua ficha foi salva com sucesso!"));
        });
    }
}
