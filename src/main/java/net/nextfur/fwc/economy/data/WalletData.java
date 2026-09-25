package net.nextfur.fwc.economy.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record WalletData(UUID walletId, UUID ownerUuid, String ownerName, long balanceCents) {
    public static final Codec<WalletData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("wallet_id").forGetter(WalletData::walletId),
                    UUIDUtil.CODEC.fieldOf("owner_uuid").forGetter(WalletData::ownerUuid),
                    Codec.STRING.fieldOf("owner_name").forGetter(WalletData::ownerName),
                    Codec.LONG.fieldOf("balance_cents").forGetter(WalletData::balanceCents)
            ).apply(instance, WalletData::new)
    );

    public static final StreamCodec<ByteBuf, WalletData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, WalletData::walletId,
            UUIDUtil.STREAM_CODEC, WalletData::ownerUuid,
            ByteBufCodecs.STRING_UTF8, WalletData::ownerName,
            ByteBufCodecs.VAR_LONG, WalletData::balanceCents,
            WalletData::new
    );

    public static WalletData createNew(UUID ownerUuid, String ownerName) {
        return new WalletData(UUID.randomUUID(), ownerUuid, ownerName, 0L);
    }

    public WalletData withBalance(long newBalance) {
        return new WalletData(walletId, ownerUuid, ownerName, newBalance);
    }

    public WalletData withOwner(UUID newOwnerUuid, String newOwnerName) {
        return new WalletData(walletId, newOwnerUuid, newOwnerName, balanceCents);
    }
}
