package net.nextfur.fwc.network.furguard;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.FwMain;

import java.util.List;

public class ModListRequestPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "server_modlist_request");
    public static final CustomPacketPayload.Type<ModListRequestPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ModListRequestPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new ModListRequestPacket()
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @OnlyIn(Dist.CLIENT)
    public static void handle(ModListRequestPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        List<String> modList = ModList.get()
                .getMods()
                .stream()
                .map(modInfo -> modInfo.getModId() + "@" + modInfo.getVersion().toString())
                .toList();

        PacketDistributor.sendToServer(new ModListPacket(
                mc.getUser().getName(),
                modList
        ));
    }
}
