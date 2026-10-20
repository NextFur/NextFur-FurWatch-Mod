package net.nextfur.fwc.persona;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.init.FwAttachments;
import net.nextfur.fwc.mixin.MobEffectInstanceAccessor;

@EventBusSubscriber(modid = FwMain.MODID)
public class PersonaEvents {
    public static final ThreadLocal<Player> CURRENT_ENCHANTING_PLAYER = new ThreadLocal<>();

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PersonaData data = PersonaService.getPersonaData(player);
            PersonaService.applyAll(player, data, true);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PersonaData data = PersonaService.getPersonaData(player);
            PersonaService.applyAll(player, data, true);
            player.setHealth(player.getMaxHealth());
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PersonaData data = PersonaService.getPersonaData(player);
            PersonaService.applyAll(player, data, true);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newPlayer) {
            PersonaData originalData = PersonaService.getPersonaData(event.getOriginal());
            newPlayer.setData(FwAttachments.PERSONA_DATA.get(), originalData);
            PersonaService.applyAll(newPlayer, originalData, true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.tickCount % 20 == 0) {
                PersonaData data = PersonaService.getPersonaData(player);
                if (data.vision() != 0) {
                    PersonaService.tickVisionEffect(player, data);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerXpChange(PlayerXpEvent.XpChange event) {
        int amount = event.getAmount();
        if (amount <= 0) return;

        Player player = event.getEntity();
        PersonaData data = PersonaService.getPersonaData(player);
        float mult = data.getWisdomXpMultiplier();
        if (Math.abs(mult - 1.0f) < 1.0E-4f) {
            return;
        }

        float exact = amount * mult;
        int scaled = (int) Math.floor(exact);
        float remainder = exact - scaled;
        if (remainder > 0.0f && player.getRandom().nextFloat() < remainder) {
            scaled += 1;
        }
        event.setAmount(Math.max(0, scaled));
    }

    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (Boolean.TRUE.equals(PersonaService.APPLYING_VISION_EFFECT.get())) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null || instance.isInfiniteDuration() || instance.isAmbient()) {
            return;
        }

        // Only scale beneficial potion effects with finite duration > 20 ticks
        if (instance.getEffect().value().getCategory() != MobEffectCategory.BENEFICIAL || instance.getDuration() <= 20) {
            return;
        }

        PersonaData data = PersonaService.getPersonaData(player);
        float durationMult = data.getWisdomPotionDurationMultiplier();
        if (Math.abs(durationMult - 1.0f) < 1.0E-4f) {
            return;
        }

        int newDuration = Math.max(20, Math.round(instance.getDuration() * durationMult));
        ((MobEffectInstanceAccessor) instance).furwatch$setDuration(newDuration);
    }

    @SubscribeEvent
    public static void onEnchantmentLevelSet(EnchantmentLevelSetEvent event) {
        int currentLevel = event.getEnchantLevel();
        if (currentLevel <= 0) return;

        Player player = CURRENT_ENCHANTING_PLAYER.get();
        if (player == null) return;

        PersonaData data = PersonaService.getPersonaData(player);
        if (data.wisdom() >= 3) {
            // Enchanting Affinity: lowers the XP level requirement in the Enchanting Table by 25%
            int discounted = Math.max(1, (int) Math.floor(currentLevel * 0.75f));
            event.setEnchantLevel(discounted);
        } else if (data.wisdom() <= -3) {
            int increased = Math.min(30, (int) Math.ceil(currentLevel * 1.25f));
            event.setEnchantLevel(increased);
        }
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (event.isCriticalHit()) {
            return;
        }

        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        PersonaData data = PersonaService.getPersonaData(player);
        float critChance = data.getCharismaGroundCritChance();
        if (critChance <= 0.0f) {
            return;
        }

        // Require reasonably charged melee swing so spam-clicking doesn't abuse crits
        if (player.getAttackStrengthScale(0.5F) >= 0.85F) {
            if (player.getRandom().nextFloat() < critChance) {
                event.setCriticalHit(true);
                event.setDamageMultiplier(Math.max(event.getDamageMultiplier(), 1.5F));
            }
        }
    }
}
