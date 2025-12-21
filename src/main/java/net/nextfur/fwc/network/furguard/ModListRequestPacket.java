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
import net.nextfur.fwc.FwMain; // Assumindo que você tem um LOGGER aqui

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

        List<String> loadedModList = ModList.get()
                .getMods()
                .stream()
                .map(modInfo -> modInfo.getModId() + "@" + modInfo.getVersion().toString())
                .toList();

        Map<String, String> modFileHashes = new HashMap<>();
        Path modsDir = mc.gameDirectory.toPath().resolve("mods");

        if (Files.isDirectory(modsDir)) {
            try (Stream<Path> stream = Files.list(modsDir)) {
                stream.filter(Files::isRegularFile)
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            String hash = getFileHash(path);
                            if (hash != null) {
                                modFileHashes.put(fileName, hash);
                            }
                        });
            } catch (IOException e) {
                FwMain.LOGGER.error("Falha ao escanear diretório de mods para anti-cheat", e);
            }
        }

        PacketDistributor.sendToServer(new ModListPacket(
                mc.getUser().getName(),
                loadedModList,
                modFileHashes
        ));
    }

    @OnlyIn(Dist.CLIENT)
    private static String getFileHash(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder(2 * hashBytes.length);
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException | IOException e) {
            FwMain.LOGGER.error("Falha ao calcular hash do arquivo: {}", path.getFileName(), e);
            return null;
        }
    }
}