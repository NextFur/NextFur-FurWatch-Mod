package net.nextfur.fwc.pda.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record PdaMessage(
        UUID id,
        UUID senderUuid,
        String senderName,
        UUID recipientUuid,
        String recipientName,
        String content,
        long timestamp
) {
    public static final Codec<PdaMessage> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("id").forGetter(PdaMessage::id),
                    UUIDUtil.CODEC.fieldOf("sender_uuid").forGetter(PdaMessage::senderUuid),
                    Codec.STRING.fieldOf("sender_name").forGetter(PdaMessage::senderName),
                    UUIDUtil.CODEC.fieldOf("recipient_uuid").forGetter(PdaMessage::recipientUuid),
                    Codec.STRING.fieldOf("recipient_name").forGetter(PdaMessage::recipientName),
                    Codec.STRING.fieldOf("content").forGetter(PdaMessage::content),
                    Codec.LONG.fieldOf("timestamp").forGetter(PdaMessage::timestamp)
            ).apply(instance, PdaMessage::new)
    );

    public static final StreamCodec<ByteBuf, PdaMessage> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {
                UUIDUtil.STREAM_CODEC.encode(buf, msg.id);
                UUIDUtil.STREAM_CODEC.encode(buf, msg.senderUuid);
                ByteBufCodecs.STRING_UTF8.encode(buf, msg.senderName);
                UUIDUtil.STREAM_CODEC.encode(buf, msg.recipientUuid);
                ByteBufCodecs.STRING_UTF8.encode(buf, msg.recipientName);
                ByteBufCodecs.STRING_UTF8.encode(buf, msg.content);
                ByteBufCodecs.VAR_LONG.encode(buf, msg.timestamp);
            },
            buf -> new PdaMessage(
                    UUIDUtil.STREAM_CODEC.decode(buf),
                    UUIDUtil.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    UUIDUtil.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf)
            )
    );
}
