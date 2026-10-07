package net.nextfur.fwc.pda.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record PdaContact(UUID uuid, String name) {
    public static final Codec<PdaContact> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("uuid").forGetter(PdaContact::uuid),
                    Codec.STRING.fieldOf("name").forGetter(PdaContact::name)
            ).apply(instance, PdaContact::new)
    );

    public static final StreamCodec<ByteBuf, PdaContact> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PdaContact::uuid,
            ByteBufCodecs.STRING_UTF8, PdaContact::name,
            PdaContact::new
    );
}
