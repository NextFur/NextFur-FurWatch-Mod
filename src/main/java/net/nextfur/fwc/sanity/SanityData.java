package net.nextfur.fwc.sanity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record SanityData(
        float sanity,
        List<Long> playerKillTimestamps,
        List<Long> animalKillTimestamps,
        long lastViolentActionTimestamp,
        long lastPlayerHitTimestamp,
        int recentPlayerHits
) {
    public static final float MAX_SANITY = 100.0f;
    public static final float MIN_SANITY = 0.0f;

    public static final SanityData DEFAULT = new SanityData(
            MAX_SANITY,
            List.of(),
            List.of(),
            0L,
            0L,
            0
    );

    public static final Codec<SanityData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("sanity", MAX_SANITY).forGetter(SanityData::sanity),
            Codec.LONG.listOf().optionalFieldOf("playerKillTimestamps", List.of()).forGetter(SanityData::playerKillTimestamps),
            Codec.LONG.listOf().optionalFieldOf("animalKillTimestamps", List.of()).forGetter(SanityData::animalKillTimestamps),
            Codec.LONG.optionalFieldOf("lastViolentActionTimestamp", 0L).forGetter(SanityData::lastViolentActionTimestamp),
            Codec.LONG.optionalFieldOf("lastPlayerHitTimestamp", 0L).forGetter(SanityData::lastPlayerHitTimestamp),
            Codec.INT.optionalFieldOf("recentPlayerHits", 0).forGetter(SanityData::recentPlayerHits)
    ).apply(instance, SanityData::new));

    public static final StreamCodec<ByteBuf, SanityData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                ByteBufCodecs.FLOAT.encode(buf, data.sanity);
                ByteBufCodecs.VAR_INT.encode(buf, data.playerKillTimestamps.size());
                for (long ts : data.playerKillTimestamps) {
                    ByteBufCodecs.VAR_LONG.encode(buf, ts);
                }
                ByteBufCodecs.VAR_INT.encode(buf, data.animalKillTimestamps.size());
                for (long ts : data.animalKillTimestamps) {
                    ByteBufCodecs.VAR_LONG.encode(buf, ts);
                }
                ByteBufCodecs.VAR_LONG.encode(buf, data.lastViolentActionTimestamp);
                ByteBufCodecs.VAR_LONG.encode(buf, data.lastPlayerHitTimestamp);
                ByteBufCodecs.VAR_INT.encode(buf, data.recentPlayerHits);
            },
            buf -> {
                float sanity = ByteBufCodecs.FLOAT.decode(buf);
                int pSize = ByteBufCodecs.VAR_INT.decode(buf);
                List<Long> pKills = new ArrayList<>(pSize);
                for (int i = 0; i < pSize; i++) {
                    pKills.add(ByteBufCodecs.VAR_LONG.decode(buf));
                }
                int aSize = ByteBufCodecs.VAR_INT.decode(buf);
                List<Long> aKills = new ArrayList<>(aSize);
                for (int i = 0; i < aSize; i++) {
                    aKills.add(ByteBufCodecs.VAR_LONG.decode(buf));
                }
                long lastViolent = ByteBufCodecs.VAR_LONG.decode(buf);
                long lastHit = ByteBufCodecs.VAR_LONG.decode(buf);
                int hits = ByteBufCodecs.VAR_INT.decode(buf);
                return new SanityData(sanity, pKills, aKills, lastViolent, lastHit, hits);
            }
    );

    public SanityData {
        sanity = Mth.clamp(sanity, MIN_SANITY, MAX_SANITY);
        playerKillTimestamps = playerKillTimestamps != null
                ? Collections.unmodifiableList(new ArrayList<>(playerKillTimestamps))
                : List.of();
        animalKillTimestamps = animalKillTimestamps != null
                ? Collections.unmodifiableList(new ArrayList<>(animalKillTimestamps))
                : List.of();
        recentPlayerHits = Math.max(0, recentPlayerHits);
    }

    public SanityData withSanity(float newSanity) {
        return new SanityData(
                Mth.clamp(newSanity, MIN_SANITY, MAX_SANITY),
                playerKillTimestamps,
                animalKillTimestamps,
                lastViolentActionTimestamp,
                lastPlayerHitTimestamp,
                recentPlayerHits
        );
    }
}
