package net.nextfur.fwc.pda.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record PdaNote(UUID id, String title, String content, long timestamp) {
    public static final Codec<PdaNote> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("id").forGetter(PdaNote::id),
                    Codec.STRING.fieldOf("title").forGetter(PdaNote::title),
                    Codec.STRING.fieldOf("content").forGetter(PdaNote::content),
                    Codec.LONG.fieldOf("timestamp").forGetter(PdaNote::timestamp)
            ).apply(instance, PdaNote::new)
    );

    public static final StreamCodec<ByteBuf, PdaNote> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PdaNote::id,
            ByteBufCodecs.STRING_UTF8, PdaNote::title,
            ByteBufCodecs.STRING_UTF8, PdaNote::content,
            ByteBufCodecs.VAR_LONG, PdaNote::timestamp,
            PdaNote::new
    );
}
