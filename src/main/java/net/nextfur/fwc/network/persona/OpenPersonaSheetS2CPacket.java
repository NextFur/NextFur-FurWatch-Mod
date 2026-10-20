package net.nextfur.fwc.network.persona;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.client.gui.PersonaClientHelper;
import net.nextfur.fwc.persona.PersonaData;

import java.util.UUID;

public record OpenPersonaSheetS2CPacket(
        UUID targetUuid,
        String targetName,
        PersonaData data,
        boolean adminEditMode
) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "open_persona_sheet");
    public static final Type<OpenPersonaSheetS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, OpenPersonaSheetS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                UUIDUtil.STREAM_CODEC.encode(buf, pkt.targetUuid);
                ByteBufCodecs.STRING_UTF8.encode(buf, pkt.targetName);
                PersonaData.STREAM_CODEC.encode(buf, pkt.data);
                ByteBufCodecs.BOOL.encode(buf, pkt.adminEditMode);
            },
            buf -> new OpenPersonaSheetS2CPacket(
                    UUIDUtil.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    PersonaData.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenPersonaSheetS2CPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> PersonaClientHelper.openScreen(
                packet.targetUuid(),
                packet.targetName(),
                packet.data(),
                packet.adminEditMode()
        ));
    }
}
