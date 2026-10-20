package net.nextfur.fwc.persona;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PersonaData(
        int strength,
        int vision,
        int agility,
        int endurance,
        int wisdom,
        int charisma,
        int availablePoints,
        boolean locked
) {
    public static final int DEFAULT_INITIAL_POINTS = 4;

    public static final PersonaData DEFAULT = new PersonaData(
            0, 0, 0, 0, 0, 0, DEFAULT_INITIAL_POINTS, false
    );

    public static final Codec<PersonaData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("strength", 0).forGetter(PersonaData::strength),
            Codec.INT.optionalFieldOf("vision", 0).forGetter(PersonaData::vision),
            Codec.INT.optionalFieldOf("agility", 0).forGetter(PersonaData::agility),
            Codec.INT.optionalFieldOf("endurance", 0).forGetter(PersonaData::endurance),
            Codec.INT.optionalFieldOf("wisdom", 0).forGetter(PersonaData::wisdom),
            Codec.INT.optionalFieldOf("charisma", 0).forGetter(PersonaData::charisma),
            Codec.INT.optionalFieldOf("availablePoints", DEFAULT_INITIAL_POINTS).forGetter(PersonaData::availablePoints),
            Codec.BOOL.optionalFieldOf("locked", false).forGetter(PersonaData::locked)
    ).apply(instance, PersonaData::new));

    public static final StreamCodec<ByteBuf, PersonaData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                ByteBufCodecs.VAR_INT.encode(buf, data.strength);
                ByteBufCodecs.VAR_INT.encode(buf, data.vision);
                ByteBufCodecs.VAR_INT.encode(buf, data.agility);
                ByteBufCodecs.VAR_INT.encode(buf, data.endurance);
                ByteBufCodecs.VAR_INT.encode(buf, data.wisdom);
                ByteBufCodecs.VAR_INT.encode(buf, data.charisma);
                ByteBufCodecs.VAR_INT.encode(buf, data.availablePoints);
                ByteBufCodecs.BOOL.encode(buf, data.locked);
            },
            buf -> new PersonaData(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            )
    );

    public PersonaData {
        strength = PersonaAttribute.STRENGTH.clampLevel(strength);
        vision = PersonaAttribute.VISION.clampLevel(vision);
        agility = PersonaAttribute.AGILITY.clampLevel(agility);
        endurance = PersonaAttribute.ENDURANCE.clampLevel(endurance);
        wisdom = PersonaAttribute.WISDOM.clampLevel(wisdom);
        charisma = PersonaAttribute.CHARISMA.clampLevel(charisma);
    }

    public int getLevel(PersonaAttribute attribute) {
        return switch (attribute) {
            case STRENGTH -> strength;
            case VISION -> vision;
            case AGILITY -> agility;
            case ENDURANCE -> endurance;
            case WISDOM -> wisdom;
            case CHARISMA -> charisma;
        };
    }

    public PersonaData withAttribute(PersonaAttribute attribute, int newLevel) {
        int clamped = attribute.clampLevel(newLevel);
        return switch (attribute) {
            case STRENGTH -> new PersonaData(clamped, vision, agility, endurance, wisdom, charisma, availablePoints, locked);
            case VISION -> new PersonaData(strength, clamped, agility, endurance, wisdom, charisma, availablePoints, locked);
            case AGILITY -> new PersonaData(strength, vision, clamped, endurance, wisdom, charisma, availablePoints, locked);
            case ENDURANCE -> new PersonaData(strength, vision, agility, clamped, wisdom, charisma, availablePoints, locked);
            case WISDOM -> new PersonaData(strength, vision, agility, endurance, clamped, charisma, availablePoints, locked);
            case CHARISMA -> new PersonaData(strength, vision, agility, endurance, wisdom, clamped, availablePoints, locked);
        };
    }

    public PersonaData withAvailablePoints(int newAvailablePoints) {
        return new PersonaData(strength, vision, agility, endurance, wisdom, charisma, newAvailablePoints, locked);
    }

    public PersonaData withLocked(boolean newLocked) {
        return new PersonaData(strength, vision, agility, endurance, wisdom, charisma, availablePoints, newLocked);
    }

    public int getSpentPoints() {
        return strength * PersonaAttribute.STRENGTH.getCostPerLevel()
                + vision * PersonaAttribute.VISION.getCostPerLevel()
                + agility * PersonaAttribute.AGILITY.getCostPerLevel()
                + endurance * PersonaAttribute.ENDURANCE.getCostPerLevel()
                + wisdom * PersonaAttribute.WISDOM.getCostPerLevel()
                + charisma * PersonaAttribute.CHARISMA.getCostPerLevel();
    }

    public int getTotalPointsPool() {
        return availablePoints + getSpentPoints();
    }

    /**
     * Strength modifier for generic.attack_damage (ADD_MULTIPLIED_TOTAL):
     * -3 -> -0.30 (0.7x), -2 -> -0.20 (0.8x), -1 -> -0.10 (0.9x),
     * 0 -> 0.0 (1.0x),
     * +1 -> +0.10 (1.1x), +2 -> +0.20 (1.2x), +3 -> +0.30 (1.3x).
     */
    public double getStrengthDamageModifier() {
        return strength * 0.10D;
    }

    /**
     * Agility modifier for generic.movement_speed (ADD_MULTIPLIED_TOTAL):
     * +1 -> +0.20 (1.2x), +2 -> +0.30 (1.3x), +3 -> +0.40 (1.4x)
     * 0 -> 0.0 (1.0x)
     * -1 -> -0.10 (0.9x), -2 -> -0.20 (0.8x), -3 -> -0.30 (0.7x).
     */
    public double getAgilitySpeedModifier() {
        if (agility > 0) {
            return 0.10D + (agility * 0.10D); // +1=0.20, +2=0.30, +3=0.40
        } else if (agility < 0) {
            return agility * 0.10D; // -1=-0.10, -2=-0.20, -3=-0.30
        }
        return 0.0D;
    }

    /**
     * Endurance modifier for generic.max_health (ADD_VALUE):
     * +1 -> +2.0 (22 HP), +2 -> +4.0 (24 HP), +3 -> +6.0 (26 HP)
     * -1 -> -2.0 (18 HP), -2 -> -4.0 (16 HP), -3 -> -6.0 (14 HP).
     */
    public double getEnduranceHealthModifier() {
        return endurance * 2.0D;
    }

    /**
     * Wisdom XP orb multiplier.
     */
    public float getWisdomXpMultiplier() {
        return switch (wisdom) {
            case -3 -> 0.50f;
            case -2 -> 0.70f;
            case -1 -> 0.85f;
            case 1 -> 1.25f;
            case 2 -> 1.50f;
            case 3 -> 1.75f;
            default -> 1.00f;
        };
    }

    /**
     * Wisdom beneficial potion duration multiplier (unlocked at level 2+ or penalized at -2/-3).
     */
    public float getWisdomPotionDurationMultiplier() {
        return switch (wisdom) {
            case -3 -> 0.70f;
            case -2 -> 0.85f;
            case 2 -> 1.25f;
            case 3 -> 1.50f;
            default -> 1.00f;
        };
    }

    /**
     * Charisma modifier for generic.luck (ADD_VALUE).
     */
    public double getCharismaLuckModifier() {
        return charisma * 1.0D;
    }

    /**
     * Charisma Villager trading discount factor (unlocked at level 2+ or penalized at -2/-3).
     * Positive = discount fraction, Negative = surcharge fraction.
     */
    public double getCharismaVillagerDiscountFactor() {
        return switch (charisma) {
            case -3 -> -0.35D;
            case -2 -> -0.20D;
            case 2 -> 0.30D;
            case 3 -> 0.425D;
            default -> 0.0D;
        };
    }

    /**
     * Charisma base critical hit chance without jumping (unlocked at level 3).
     */
    public float getCharismaGroundCritChance() {
        return charisma >= 3 ? 0.20f : 0.0f;
    }

    /**
     * Extra bad-odds probability in /roll when Charisma is negative (-1: 5%, -2: 10%, -3: 20%).
     * Positive Charisma does not increase /roll luck.
     */
    public float getCharismaRollBadOdds() {
        return switch (charisma) {
            case -1 -> 0.05f;
            case -2 -> 0.10f;
            case -3 -> 0.20f;
            default -> 0.0f;
        };
    }
}
