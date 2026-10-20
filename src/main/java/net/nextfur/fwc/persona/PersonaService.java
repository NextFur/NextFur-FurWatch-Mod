package net.nextfur.fwc.persona;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.init.FwAttachments;

public final class PersonaService {
    public static final ResourceLocation STRENGTH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "persona_strength");
    public static final ResourceLocation AGILITY_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "persona_agility");
    public static final ResourceLocation ENDURANCE_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "persona_endurance");
    public static final ResourceLocation CHARISMA_LUCK_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "persona_charisma_luck");

    public static final ThreadLocal<Boolean> APPLYING_VISION_EFFECT = ThreadLocal.withInitial(() -> false);

    private PersonaService() {
    }

    public static PersonaData getPersonaData(Player player) {
        PersonaData data = player.getData(FwAttachments.PERSONA_DATA.get());
        return data != null ? data : PersonaData.DEFAULT;
    }

    public static void setPersonaData(ServerPlayer player, PersonaData data) {
        PersonaData previous = getPersonaData(player);
        player.setData(FwAttachments.PERSONA_DATA.get(), data);
        applyAll(player, data, previous.vision() != data.vision());
    }

    public static void applyAll(ServerPlayer player, PersonaData data, boolean visionChanged) {
        float previousMaxHealth = player.getMaxHealth();
        boolean wasFullHealth = player.getHealth() >= previousMaxHealth - 0.01f;

        // 1. Strength -> generic.attack_damage (ADD_MULTIPLIED_TOTAL)
        applyModifier(
                player.getAttribute(Attributes.ATTACK_DAMAGE),
                STRENGTH_MODIFIER_ID,
                data.getStrengthDamageModifier(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        // 2. Agility -> generic.movement_speed (ADD_MULTIPLIED_TOTAL)
        applyModifier(
                player.getAttribute(Attributes.MOVEMENT_SPEED),
                AGILITY_MODIFIER_ID,
                data.getAgilitySpeedModifier(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        // 3. Endurance -> generic.max_health (ADD_VALUE)
        applyModifier(
                player.getAttribute(Attributes.MAX_HEALTH),
                ENDURANCE_MODIFIER_ID,
                data.getEnduranceHealthModifier(),
                AttributeModifier.Operation.ADD_VALUE
        );

        // 4. Charisma -> generic.luck (ADD_VALUE)
        applyModifier(
                player.getAttribute(Attributes.LUCK),
                CHARISMA_LUCK_MODIFIER_ID,
                data.getCharismaLuckModifier(),
                AttributeModifier.Operation.ADD_VALUE
        );

        float newMaxHealth = player.getMaxHealth();
        if (player.getHealth() > newMaxHealth) {
            player.setHealth(newMaxHealth);
        } else if (wasFullHealth && newMaxHealth > previousMaxHealth) {
            player.setHealth(newMaxHealth);
        }

        // 5. Vision -> Night Vision (+1) or Darkness (-1)
        if (visionChanged) {
            clearPersonaVisionEffects(player, data.vision());
        }
        tickVisionEffect(player, data);
    }

    private static void applyModifier(
            AttributeInstance instance,
            ResourceLocation id,
            double amount,
            AttributeModifier.Operation operation
    ) {
        if (instance == null) return;
        instance.removeModifier(id);
        if (Math.abs(amount) > 1.0E-6D) {
            instance.addPermanentModifier(new AttributeModifier(id, amount, operation));
        }
    }

    private static void clearPersonaVisionEffects(ServerPlayer player, int newVisionLevel) {
        if (newVisionLevel != 1) {
            MobEffectInstance nv = player.getEffect(MobEffects.NIGHT_VISION);
            if (nv != null && nv.isAmbient() && !nv.isVisible()) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }
        if (newVisionLevel != -1) {
            MobEffectInstance dk = player.getEffect(MobEffects.DARKNESS);
            if (dk != null && dk.isAmbient() && !dk.isVisible()) {
                player.removeEffect(MobEffects.DARKNESS);
            }
        }
    }

    public static void tickVisionEffect(ServerPlayer player, PersonaData data) {
        int vision = data.vision();
        if (vision == 1) {
            MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
            // Night Vision flickers below 200 ticks, so refresh whenever below 260 ticks
            if (current == null || (!current.isInfiniteDuration() && current.getDuration() < 260)) {
                APPLYING_VISION_EFFECT.set(true);
                try {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 340, 0, true, false, true));
                } finally {
                    APPLYING_VISION_EFFECT.set(false);
                }
            }
        } else if (vision == -1) {
            MobEffectInstance current = player.getEffect(MobEffects.DARKNESS);
            if (current == null || (!current.isInfiniteDuration() && current.getDuration() < 80)) {
                APPLYING_VISION_EFFECT.set(true);
                try {
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 160, 0, true, false, true));
                } finally {
                    APPLYING_VISION_EFFECT.set(false);
                }
            }
        }
    }
}
