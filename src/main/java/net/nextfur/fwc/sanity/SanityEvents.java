package net.nextfur.fwc.sanity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.init.FwAttachments;

@EventBusSubscriber(modid = FwMain.MODID)
public class SanityEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SanityData data = SanityService.getSanityData(player);
            SanityService.sync(player, data.sanity(), false);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SanityData data = SanityService.getSanityData(player);
            SanityService.sync(player, data.sanity(), false);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SanityData data = SanityService.getSanityData(player);
            SanityService.sync(player, data.sanity(), false);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newPlayer) {
            if (event.isWasDeath()) {
                // Reset sanity to 100% upon death
                newPlayer.setData(FwAttachments.SANITY_DATA.get(), SanityData.DEFAULT);
                SanityService.sync(newPlayer, SanityData.MAX_SANITY, false);
            } else {
                // Keep sanity when returning from the End dimension
                SanityData originalData = SanityService.getSanityData(event.getOriginal());
                newPlayer.setData(FwAttachments.SANITY_DATA.get(), originalData);
                SanityService.sync(newPlayer, originalData.sanity(), false);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }

        ServerPlayer killer = resolvePlayerKiller(victim, event.getSource().getEntity());
        if (killer == null || killer == victim) {
            return;
        }

        if (victim instanceof Player) {
            SanityService.recordPlayerKill(killer);
        } else if (SanityService.isAnimalEntity(victim)) {
            SanityService.recordAnimalKill(killer);
        }
    }

    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer target) || target.level().isClientSide()) {
            return;
        }

        if (event.getNewDamage() <= 0.5f || target.isDeadOrDying()) {
            return;
        }

        Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity instanceof ServerPlayer attacker && attacker != target) {
            SanityService.recordPlayerHit(attacker);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.tickCount % 20 == 0) {
                SanityService.tickServerPlayer(player);
            }
        }
    }

    private static ServerPlayer resolvePlayerKiller(LivingEntity victim, Entity directSource) {
        if (directSource instanceof ServerPlayer player) {
            return player;
        }
        LivingEntity credit = victim.getKillCredit();
        if (credit instanceof ServerPlayer player) {
            return player;
        }
        return null;
    }
}
