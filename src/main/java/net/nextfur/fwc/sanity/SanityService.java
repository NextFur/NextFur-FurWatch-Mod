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

    // Low sanity effect thresholds (35% down to 10%, increasing exponentially)
    public static final float LOW_SANITY_START = 35.0f;
    public static final float LOW_SANITY_CRITICAL = 10.0f;
    private static final double EXP_STEEPNESS = 3.0D;
    private static final double EXP_DENOMINATOR = Math.exp(EXP_STEEPNESS) - 1.0D;

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
     * Above 35% sanity -> 0.0.
     * Between 35% and 10% -> increases exponentially from 0.0 to 1.0.
     * At or below 10% -> 1.0 (maximum activity).
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

        // 1. Passive recovery: 0% -> 100% over 6 hours when not killing/hitting anything
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

        // 2. Low-sanity random weakening effects (35% down to 10%, increasing exponentially)
        if (sanity < LOW_SANITY_START && !player.isCreative() && !player.isSpectator()) {
            tickLowSanityWeakeningEffects(player, sanity);
        }
    }

    private static void tickLowSanityWeakeningEffects(ServerPlayer player, float sanity) {
        float intensity = getInsanityIntensity(sanity);
        if (intensity <= 0.0f) {
            return;
        }

        RandomSource random = player.getRandom();
        // Exponentially increasing chance per second:
        // ~0.8%/sec near 34%, ~2.5%/sec at 20%, ~8.3%/sec at <=10%
        float triggerChance = 0.008f + (0.075f * intensity);
        if (random.nextFloat() >= triggerChance) {
            return;
        }

        // Select a weakening effect weighted by current insanity intensity
        int roll = random.nextInt(100);
        Holder<MobEffect> chosenEffect;
        int durationTicks;
        int amplifier = 0;

        if (roll < 30) {
            // Weakness
            chosenEffect = MobEffects.WEAKNESS;
            durationTicks = Mth.lerpInt(intensity, 120, 320); // 6s - 16s
            if (intensity > 0.75f && random.nextFloat() < 0.45f) {
                amplifier = 1;
            }
        } else if (roll < 55) {
            // Slowness
            chosenEffect = MobEffects.MOVEMENT_SLOWDOWN;
            durationTicks = Mth.lerpInt(intensity, 80, 220); // 4s - 11s
            if (intensity > 0.85f && random.nextFloat() < 0.35f) {
                amplifier = 1;
            }
        } else if (roll < 75) {
            // Mining Fatigue
            chosenEffect = MobEffects.DIG_SLOWDOWN;
            durationTicks = Mth.lerpInt(intensity, 120, 280); // 6s - 14s
        } else if (roll < 88) {
            // Hunger
            chosenEffect = MobEffects.HUNGER;
            durationTicks = Mth.lerpInt(intensity, 140, 300); // 7s - 15s
        } else if (sanity <= 28.0f && roll < 95) {
            // Darkness pulse
            chosenEffect = MobEffects.DARKNESS;
            durationTicks = Mth.lerpInt(intensity, 80, 180); // 4s - 9s
        } else if (sanity <= 18.0f) {
            // Brief Blindness or Nausea at critical sanity
            if (random.nextBoolean()) {
                chosenEffect = MobEffects.BLINDNESS;
                durationTicks = Mth.lerpInt(intensity, 45, 90); // 2.2s - 4.5s
            } else {
                chosenEffect = MobEffects.CONFUSION;
                durationTicks = Mth.lerpInt(intensity, 100, 160); // 5s - 8s
            }
        } else {
            chosenEffect = MobEffects.WEAKNESS;
            durationTicks = 140;
        }

        // Apply with visible=false and showIcon=false so it infringes naturally without HUD clutter
        player.addEffect(new MobEffectInstance(chosenEffect, durationTicks, amplifier, false, false, false));
    }

    public static void clearSanityDebuffs(ServerPlayer player) {
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
