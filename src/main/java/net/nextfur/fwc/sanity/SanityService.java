package net.nextfur.fwc.sanity;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.init.FwAttachments;
import net.nextfur.fwc.network.sanity.SanitySyncS2CPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SanityService {
    public static final float MAX_SANITY = SanityData.MAX_SANITY;
    public static final float MIN_SANITY = SanityData.MIN_SANITY;

    // 0% -> 100% restored over 18 hours (64,800 seconds) of not killing anything
    public static final int FULL_RECOVERY_SECONDS = 18 * 3600;
    public static final float RECOVERY_PER_SECOND = 100.0f / (float) FULL_RECOVERY_SECONDS;
    public static final long RECOVERY_IDLE_DELAY_MS = 10_000L;

    // Time windows for kill penalty curves
    public static final long WINDOW_10_MIN_MS = 10L * 60L * 1000L;
    public static final long WINDOW_30_MIN_MS = 30L * 60L * 1000L;
    public static final long WINDOW_60_MIN_MS = 60L * 60L * 1000L;

    // Person (Player) kill penalties
    public static final float PLAYER_BASE_PENALTY = 10.0f;
    public static final float PLAYER_HOUR_CONST_PENALTY = 20.0f;

    // Animal kill penalties
    public static final float ANIMAL_BASE_PENALTY = 5.0f;
    public static final float ANIMAL_HOUR_CONST_PENALTY = 6.0f;

    // Non-lethal player hit penalties
    public static final float PLAYER_HIT_BASE_PENALTY = 1.0f;
    public static final long PLAYER_HIT_COOLDOWN_MS = 3_000L;

    // Low sanity effect thresholds (50% down to 1%, increasing exponentially)
    public static final float LOW_SANITY_START = 50.0f;
    public static final float LOW_SANITY_CRITICAL = 1.0f;
    private static final double EXP_STEEPNESS = 3.0D;
    private static final double EXP_DENOMINATOR = Math.exp(EXP_STEEPNESS) - 1.0D;

    // Long minimum interval between server-side low-sanity weakening activities per player
    // (6 minutes near 50% sanity down to 2 minutes minimum at <=1% sanity)
    public static final long MIN_ACTIVITY_INTERVAL_MAX_MS = 360_000L;
    public static final long MIN_ACTIVITY_INTERVAL_MIN_MS = 120_000L;
    private static final Map<UUID, Long> LAST_WEAKENING_ACTIVITY_MS = new ConcurrentHashMap<>();

    private SanityService() {
    }

    public static SanityData getSanityData(Player player) {
        SanityData data = player.getData(FwAttachments.SANITY_DATA.get());
        return data != null ? data : SanityData.DEFAULT;
    }

    public static void setSanityData(ServerPlayer player, SanityData data, boolean triggerSpike) {
        SanityData previous = getSanityData(player);
        player.setData(FwAttachments.SANITY_DATA.get(), data);
        if (previous.sanity() < LOW_SANITY_START && data.sanity() >= LOW_SANITY_START) {
            clearSanityDebuffs(player);
        }
        sync(player, data.sanity(), triggerSpike);
    }

    public static void sync(ServerPlayer player, float sanity, boolean triggerSpike) {
        PacketDistributor.sendToPlayer(player, new SanitySyncS2CPacket(sanity, triggerSpike));
    }

    /**
     * Exponential low-sanity activity factor in [0.0, 1.0].
     * Above 50% sanity -> 0.0.
     * Between 50% and 1% -> increases exponentially from 0.0 to 1.0.
     * At or below 1% -> 1.0 (maximum activity).
     */
    public static float getInsanityIntensity(float sanity) {
        if (sanity >= LOW_SANITY_START) {
            return 0.0f;
        }
        float linearProgress = Mth.clamp(
                (LOW_SANITY_START - sanity) / (LOW_SANITY_START - LOW_SANITY_CRITICAL),
                0.0f,
                1.0f
        );
        double expValue = (Math.exp(EXP_STEEPNESS * linearProgress) - 1.0D) / EXP_DENOMINATOR;
        return Mth.clamp((float) expValue, 0.0f, 1.0f);
    }

    public static boolean isAnimalEntity(Entity entity) {
        if (entity == null || entity instanceof Player || entity instanceof Enemy) {
            return false;
        }
        return entity instanceof Animal
                || entity instanceof WaterAnimal
                || entity instanceof AmbientCreature;
    }

    /**
     * Calculates the sanity penalty for a kill based on recent kill history:
     * - No kills in the last 60 minutes: full basePenalty (10% for player, 5% for animal)
     * - Next kill within 10 minutes: decreases on a cubic curve: basePenalty / (killsIn10Min + 1)^3
     * - Next kill within 10-30 minutes: decreases on a quadratic curve: basePenalty / (killsIn30Min + 1)^2
     * - Next kill within 30-60 minutes (same hour): fixed constant (8% for player, 3% for animal)
     */
    public static float calculateKillPenalty(
            List<Long> prunedTimestamps,
            long now,
            float basePenalty,
            float hourConstantPenalty
    ) {
        if (prunedTimestamps.isEmpty()) {
            return basePenalty;
        }

        long lastKillTime = prunedTimestamps.get(prunedTimestamps.size() - 1);
        long deltaMs = Math.max(0L, now - lastKillTime);

        if (deltaMs > WINDOW_60_MIN_MS) {
            return basePenalty;
        }

        int killsIn10Min = countWithinWindow(prunedTimestamps, now, WINDOW_10_MIN_MS);
        int killsIn30Min = countWithinWindow(prunedTimestamps, now, WINDOW_30_MIN_MS);

        if (deltaMs <= WINDOW_10_MIN_MS) {
            int n = Math.max(1, killsIn10Min);
            return (float) (basePenalty / Math.pow(n + 1, 3.0D));
        } else if (deltaMs <= WINDOW_30_MIN_MS) {
            int n = Math.max(1, killsIn30Min);
            return (float) (basePenalty / Math.pow(n + 1, 2.0D));
        } else {
            return hourConstantPenalty;
        }
    }

    public static float previewNextPlayerKillPenalty(SanityData data, long now) {
        List<Long> pruned = pruneTimestamps(data.playerKillTimestamps(), now, WINDOW_60_MIN_MS);
        return calculateKillPenalty(pruned, now, PLAYER_BASE_PENALTY, PLAYER_HOUR_CONST_PENALTY);
    }

    public static float previewNextAnimalKillPenalty(SanityData data, long now) {
        List<Long> pruned = pruneTimestamps(data.animalKillTimestamps(), now, WINDOW_60_MIN_MS);
        return calculateKillPenalty(pruned, now, ANIMAL_BASE_PENALTY, ANIMAL_HOUR_CONST_PENALTY);
    }

    public static void recordPlayerKill(ServerPlayer killer) {
        long now = System.currentTimeMillis();
        SanityData current = getSanityData(killer);

        List<Long> prunedPlayerKills = pruneTimestamps(current.playerKillTimestamps(), now, WINDOW_60_MIN_MS);
        List<Long> prunedAnimalKills = pruneTimestamps(current.animalKillTimestamps(), now, WINDOW_60_MIN_MS);

        float penalty = calculateKillPenalty(prunedPlayerKills, now, PLAYER_BASE_PENALTY, PLAYER_HOUR_CONST_PENALTY);
        prunedPlayerKills.add(now);

        float newSanity = Mth.clamp(current.sanity() - penalty, MIN_SANITY, MAX_SANITY);
        SanityData updated = new SanityData(
                newSanity,
                prunedPlayerKills,
                prunedAnimalKills,
                now,
                current.lastPlayerHitTimestamp(),
                current.recentPlayerHits()
        );
        setSanityData(killer, updated, newSanity < LOW_SANITY_START);
    }

    public static void recordAnimalKill(ServerPlayer killer) {
        long now = System.currentTimeMillis();
        SanityData current = getSanityData(killer);

        List<Long> prunedPlayerKills = pruneTimestamps(current.playerKillTimestamps(), now, WINDOW_60_MIN_MS);
        List<Long> prunedAnimalKills = pruneTimestamps(current.animalKillTimestamps(), now, WINDOW_60_MIN_MS);

        float penalty = calculateKillPenalty(prunedAnimalKills, now, ANIMAL_BASE_PENALTY, ANIMAL_HOUR_CONST_PENALTY);
        prunedAnimalKills.add(now);

        float newSanity = Mth.clamp(current.sanity() - penalty, MIN_SANITY, MAX_SANITY);
        SanityData updated = new SanityData(
                newSanity,
                prunedPlayerKills,
                prunedAnimalKills,
                now,
                current.lastPlayerHitTimestamp(),
                current.recentPlayerHits()
        );
        setSanityData(killer, updated, newSanity < LOW_SANITY_START);
    }

    public static void recordPlayerHit(ServerPlayer attacker) {
        long now = System.currentTimeMillis();
        SanityData current = getSanityData(attacker);

        if (now - current.lastPlayerHitTimestamp() < PLAYER_HIT_COOLDOWN_MS) {
            return;
        }

        int priorHitsInWindow = (now - current.lastPlayerHitTimestamp() <= WINDOW_10_MIN_MS)
                ? current.recentPlayerHits()
                : 0;

        // Diminishes on a cubic curve during continuous combat within 10 minutes
        float penalty = (float) (PLAYER_HIT_BASE_PENALTY / Math.pow(priorHitsInWindow + 1, 3.0D));
        float newSanity = Mth.clamp(current.sanity() - penalty, MIN_SANITY, MAX_SANITY);

        SanityData updated = new SanityData(
                newSanity,
                current.playerKillTimestamps(),
                current.animalKillTimestamps(),
                now,
                now,
                priorHitsInWindow + 1
        );
        setSanityData(attacker, updated, false);
    }

    public static void tickServerPlayer(ServerPlayer player) {
        if (!player.isAlive()) {
            return;
        }

        long now = System.currentTimeMillis();
        SanityData current = getSanityData(player);
        float sanity = current.sanity();

        // 1. Passive recovery: 0% -> 100% over 18 hours when not killing/hitting anything
        if (sanity < MAX_SANITY && (now - current.lastViolentActionTimestamp() >= RECOVERY_IDLE_DELAY_MS)) {
            float recovered = Math.min(MAX_SANITY, sanity + RECOVERY_PER_SECOND);
            boolean crossedThreshold = sanity < LOW_SANITY_START && recovered >= LOW_SANITY_START;
            sanity = recovered;

            SanityData updated = current.withSanity(sanity);
            player.setData(FwAttachments.SANITY_DATA.get(), updated);

            if (crossedThreshold) {
                clearSanityDebuffs(player);
                sync(player, sanity, false);
            } else if (player.tickCount % 100 == 0 || sanity >= MAX_SANITY) {
                // Sync periodically every 5 seconds while recovering
                sync(player, sanity, false);
            }
        }

        // 2. Low-sanity random weakening effects (50% down to 1%, spaced out with long intervals)
        if (sanity < LOW_SANITY_START && !player.isCreative() && !player.isSpectator()) {
            tickLowSanityWeakeningEffects(player, sanity, now);
        }
    }

    private static void tickLowSanityWeakeningEffects(ServerPlayer player, float sanity, long now) {
        float intensity = getInsanityIntensity(sanity);
        if (intensity <= 0.0f) {
            return;
        }

        // Enforce a long minimum interval (6 min down to 2 min) so debuffs are precautionary, not annoying
        long minIntervalMs = Mth.lerpInt(
                intensity,
                (int) MIN_ACTIVITY_INTERVAL_MAX_MS,
                (int) MIN_ACTIVITY_INTERVAL_MIN_MS
        );
        long lastActivity = LAST_WEAKENING_ACTIVITY_MS.getOrDefault(player.getUUID(), 0L);
        if (locationOrTimeNotReady(now, lastActivity, minIntervalMs)) {
            return;
        }

        RandomSource random = player.getRandom();
        float triggerChance = 0.0005f + (0.0035f * intensity);
        if (random.nextFloat() >= triggerChance) {
            return;
        }

        LAST_WEAKENING_ACTIVITY_MS.put(player.getUUID(), now);

        // Select a brief weakening effect weighted by current insanity intensity
        int roll = random.nextInt(100);
        Holder<MobEffect> chosenEffect;
        int durationTicks;
        int amplifier = 0;

        if (roll < 35) {
            // Weakness
            chosenEffect = MobEffects.WEAKNESS;
            durationTicks = Mth.lerpInt(intensity, 80, 160); // 4s - 8s
            if (intensity > 0.75f && random.nextFloat() < 0.09f) {
                amplifier = 1;
            }
        } else if (roll < 62) {
            // Slowness
            chosenEffect = MobEffects.MOVEMENT_SLOWDOWN;
            durationTicks = Mth.lerpInt(intensity, 60, 140); // 3s - 7s
            if (intensity > 0.85f && random.nextFloat() < 0.07f) {
                amplifier = 1;
            }
        } else if (roll < 82) {
            // Mining Fatigue
            chosenEffect = MobEffects.DIG_SLOWDOWN;
            durationTicks = Mth.lerpInt(intensity, 80, 160); // 4s - 8s
        } else if (roll < 95 || sanity > 12.0f) {
            // Hunger
            chosenEffect = MobEffects.HUNGER;
            durationTicks = Mth.lerpInt(intensity, 100, 180); // 5s - 9s
        } else {
            // Brief Nausea at critical sanity
            chosenEffect = MobEffects.CONFUSION;
            durationTicks = Mth.lerpInt(intensity, 60, 110); // 3s - 5.5s
        }

        // Apply with visible=false and showIcon=false so it infringes naturally without HUD clutter
        player.addEffect(new MobEffectInstance(chosenEffect, durationTicks, amplifier, false, false, false));
    }

    private static boolean locationOrTimeNotReady(long now, long lastActivity, long minIntervalMs) {
        if (lastActivity == 0L) {
            return false;
        }
        return (now - lastActivity) < minIntervalMs;
    }

    public static void clearSanityDebuffs(ServerPlayer player) {
        LAST_WEAKENING_ACTIVITY_MS.remove(player.getUUID());
        removeHiddenEffectIfPresent(player, MobEffects.WEAKNESS);
        removeHiddenEffectIfPresent(player, MobEffects.MOVEMENT_SLOWDOWN);
        removeHiddenEffectIfPresent(player, MobEffects.DIG_SLOWDOWN);
        removeHiddenEffectIfPresent(player, MobEffects.HUNGER);
        removeHiddenEffectIfPresent(player, MobEffects.DARKNESS);
        removeHiddenEffectIfPresent(player, MobEffects.BLINDNESS);
        removeHiddenEffectIfPresent(player, MobEffects.CONFUSION);
    }

    private static void removeHiddenEffectIfPresent(ServerPlayer player, Holder<MobEffect> effect) {
        MobEffectInstance inst = player.getEffect(effect);
        if (inst != null && !inst.isVisible() && !inst.showIcon() && !inst.isAmbient()) {
            player.removeEffect(effect);
        }
    }

    public static List<Long> pruneTimestamps(List<Long> timestamps, long now, long maxAgeMs) {
        List<Long> pruned = new ArrayList<>();
        if (timestamps == null) {
            return pruned;
        }
        for (long ts : timestamps) {
            if (now - ts <= maxAgeMs) {
                pruned.add(ts);
            }
        }
        return pruned;
    }

    public static int countWithinWindow(List<Long> timestamps, long now, long windowMs) {
        if (timestamps == null || timestamps.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (long ts : timestamps) {
            if (now - ts <= windowMs) {
                count++;
            }
        }
        return count;
    }
}
