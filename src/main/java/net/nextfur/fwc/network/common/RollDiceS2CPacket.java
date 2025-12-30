package net.nextfur.fwc.network.common;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;

public class RollDiceS2CPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "roll_result_raw");
    public static final Type<RollDiceS2CPacket> TYPE = new Type<>(ID);

    private final String formula;
    private final int result;
    private final int sourceEntityId;

    public RollDiceS2CPacket(String formula, int result, int sourceEntityId) {
        this.formula = formula;
        this.result = result;
        this.sourceEntityId = sourceEntityId;
    }

    public static final StreamCodec<FriendlyByteBuf, RollDiceS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                buffer.writeUtf(packet.formula);
                buffer.writeInt(packet.result);
                buffer.writeInt(packet.sourceEntityId);
            },
            buffer -> new RollDiceS2CPacket(buffer.readUtf(), buffer.readInt(), buffer.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RollDiceS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            handleClient(packet.formula, packet.result, packet.sourceEntityId);
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(String formula, int result, int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(entityId);
        String name = (entity != null) ? entity.getName().getString() : "Desconhecido";

        Component msg = Component.literal("[Roll] ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(name).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" - ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(formula).withStyle(ChatFormatting.RED))
                .append(Component.literal(" : ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.valueOf(result)).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));

        mc.player.displayClientMessage(msg, false);
    }
}
