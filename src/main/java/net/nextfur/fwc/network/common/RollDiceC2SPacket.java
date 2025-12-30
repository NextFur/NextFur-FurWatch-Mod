package net.nextfur.fwc.network.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.FwMain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RollDiceC2SPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "perform_roll");
    public static final Type<RollDiceC2SPacket> TYPE = new Type<>(ID);

    private final String formula;
    private final boolean isPrivate;

    public RollDiceC2SPacket(String formula, boolean isPrivate) {
        this.formula = formula;
        this.isPrivate = isPrivate;
    }

    public static final StreamCodec<FriendlyByteBuf, RollDiceC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                buffer.writeUtf(packet.formula);
                buffer.writeBoolean(packet.isPrivate);
            },
            buffer -> new RollDiceC2SPacket(buffer.readUtf(), buffer.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RollDiceC2SPacket packet, ServerPlayer sender) {
        int result = calculateResult(packet.formula);

        RollDiceS2CPacket responsePacket = new RollDiceS2CPacket(packet.formula, result, sender.getId());

        if (packet.isPrivate) {
            PacketDistributor.sendToPlayer(sender, responsePacket);
        } else {
            PacketDistributor.sendToPlayersNear(
                    sender.serverLevel(),
                    null,
                    sender.getX(), sender.getY(), sender.getZ(),
                    32,
                    responsePacket
            );
        }

        for (ServerPlayer p : sender.serverLevel().players()) {
            if (p.hasPermissions(2) && p.distanceToSqr(sender) < 64 * 64) {
                // chat log to prevent private & public roll cheats
            }
        }
    }


    private static int calculateResult(String formula) {
        try {
            Pattern pattern = Pattern.compile("(\\d+)d(\\d+)?");
            Matcher matcher = pattern.matcher(formula.trim());

            if (matcher.find()) {
                int count = Integer.parseInt(matcher.group(1));
                int faces = Integer.parseInt(matcher.group(2));

                if (count > 10) count = 10;
                if (faces > 100) faces = 100;

                int total = 0;
                for (int i = 0; i < count; i++) {
                    total += (int) (Math.random() * faces) + 1;
                }
                return total;
            }
        } catch (Exception ignore) {}
        return 0;
    }
}
