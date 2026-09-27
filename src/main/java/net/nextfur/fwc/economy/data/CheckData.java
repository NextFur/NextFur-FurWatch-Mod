package net.nextfur.fwc.economy.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record CheckData(
        UUID checkId,
        UUID issuerUuid,
        String issuerName,
        String payee,
        long amountCents,
        long timestamp,
        boolean deposited
) {
    public static final Codec<CheckData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("check_id").forGetter(CheckData::checkId),
                    UUIDUtil.CODEC.fieldOf("issuer_uuid").forGetter(CheckData::issuerUuid),
                    Codec.STRING.fieldOf("issuer_name").forGetter(CheckData::issuerName),
                    Codec.STRING.fieldOf("payee").forGetter(CheckData::payee),
                    Codec.LONG.fieldOf("amount_cents").forGetter(CheckData::amountCents),
                    Codec.LONG.fieldOf("timestamp").forGetter(CheckData::timestamp),
                    Codec.BOOL.fieldOf("deposited").forGetter(CheckData::deposited)
            ).apply(instance, CheckData::new)
    );

    public static final StreamCodec<ByteBuf, CheckData> STREAM_CODEC = StreamCodec.of(
            (buf, val) -> {
                UUIDUtil.STREAM_CODEC.encode(buf, val.checkId());
                UUIDUtil.STREAM_CODEC.encode(buf, val.issuerUuid());
                ByteBufCodecs.STRING_UTF8.encode(buf, val.issuerName());
                ByteBufCodecs.STRING_UTF8.encode(buf, val.payee());
                buf.writeLong(val.amountCents());
                buf.writeLong(val.timestamp());
                buf.writeBoolean(val.deposited());
            },
            buf -> new CheckData(
                    UUIDUtil.STREAM_CODEC.decode(buf),
                    UUIDUtil.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    buf.readLong(),
                    buf.readLong(),
                    buf.readBoolean()
            )
    );

    public CheckData withDeposited(boolean deposited) {
        return new CheckData(checkId, issuerUuid, issuerName, payee, amountCents, timestamp, deposited);
    }
}
