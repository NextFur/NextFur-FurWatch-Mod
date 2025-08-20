package net.nextfur.fwc.network;

import net.nextfur.fwc.FwMain;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TokenPayload(String token) implements CustomPacketPayload {
    public static final Type<TokenPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "auth_token"));

    public static final StreamCodec<ByteBuf, TokenPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, 
            TokenPayload::token,   
            TokenPayload::new     
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public String getToken() {
        return this.token;
    }
}