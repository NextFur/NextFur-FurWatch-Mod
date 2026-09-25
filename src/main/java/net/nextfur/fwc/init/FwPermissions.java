package net.nextfur.fwc.init;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import net.nextfur.fwc.FwMain;

@EventBusSubscriber(modid = FwMain.MODID)
public class FwPermissions {
    public static final PermissionNode<Boolean> ADMIN = new PermissionNode<>(
            FwMain.MODID,
            "furwatch.admin",
            PermissionTypes.BOOLEAN,
            (player, playerUUID, context) -> player != null && player.hasPermissions(2)
    );

    @SubscribeEvent
    public static void onPermissionGather(PermissionGatherEvent.Nodes event) {
        event.addNodes(ADMIN);
    }

    public static boolean hasAdminPermission(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return PermissionAPI.getPermission(player, ADMIN);
        }
        return source.hasPermission(2);
    }
}
