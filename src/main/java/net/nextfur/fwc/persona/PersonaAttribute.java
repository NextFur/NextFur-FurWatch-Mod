package net.nextfur.fwc.persona;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum PersonaAttribute {
    STRENGTH(
            "strength",
            new String[]{"forca", "força", "str"},
            "Força",
            -3, 3, 1
    ),
    VISION(
            "vision",
            new String[]{"visao", "visão", "vis"},
            "Visão",
            -1, 1, 3
    ),
    AGILITY(
            "agility",
            new String[]{"agilidade", "agi"},
            "Agilidade",
            -3, 3, 1
    ),
    ENDURANCE(
            "endurance",
            new String[]{"resistencia", "resistência", "end", "vigor"},
            "Resistência",
            -3, 3, 1
    ),
    WISDOM(
            "wisdom",
            new String[]{"sabedoria", "wis"},
            "Sabedoria",
            -3, 3, 1
    ),
    CHARISMA(
            "charisma",
            new String[]{"carisma", "cha"},
            "Carisma",
            -3, 3, 1
    );

    private final String id;
    private final String[] aliases;
    private final String displayName;
    private final int minLevel;
    private final int maxLevel;
    private final int costPerLevel;

    PersonaAttribute(String id, String[] aliases, String displayName, int minLevel, int maxLevel, int costPerLevel) {
        this.id = id;
        this.aliases = aliases;
        this.displayName = displayName;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.costPerLevel = costPerLevel;
    }

    public String getId() {
        return id;
    }

    public String[] getAliases() {
        return aliases;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getCostPerLevel() {
        return costPerLevel;
    }

    public int clampLevel(int level) {
        return Math.max(minLevel, Math.min(maxLevel, level));
    }

    public static PersonaAttribute byName(String name) {
        if (name == null) return null;
        String clean = name.trim().toLowerCase(Locale.ROOT);
        for (PersonaAttribute attr : values()) {
            if (attr.id.equals(clean)) {
                return attr;
            }
            for (String alias : attr.aliases) {
                if (alias.equals(clean)) {
                    return attr;
                }
            }
        }
        return null;
    }

    public List<Component> buildTooltip(int currentLevel) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(displayName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        lines.add(Component.literal("Custo: " + costPerLevel + (costPerLevel == 1 ? " ponto por nível" : " pontos"))
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.empty());

        switch (this) {
            case STRENGTH -> {
                addTierLine(lines, currentLevel, -3, "0.7x Dano de ataque");
                addTierLine(lines, currentLevel, -2, "0.8x Dano de ataque");
                addTierLine(lines, currentLevel, -1, "0.9x Dano de ataque");
                addTierLine(lines, currentLevel, 0, "1.0x Dano padrão");
                addTierLine(lines, currentLevel, 1, "1.1x Dano de ataque");
                addTierLine(lines, currentLevel, 2, "1.2x Dano de ataque");
                addTierLine(lines, currentLevel, 3, "1.3x Dano de ataque");
            }
            case VISION -> {
                addTierLine(lines, currentLevel, -1, "Escuridão permanente");
                addTierLine(lines, currentLevel, 0, "Visão normal");
                addTierLine(lines, currentLevel, 1, "Visão noturna permanente");
            }
            case AGILITY -> {
                addTierLine(lines, currentLevel, -3, "0.7x Velocidade de movimento");
                addTierLine(lines, currentLevel, -2, "0.8x Velocidade de movimento");
                addTierLine(lines, currentLevel, -1, "0.9x Velocidade de movimento");
                addTierLine(lines, currentLevel, 0, "1.0x Velocidade padrão");
                addTierLine(lines, currentLevel, 1, "1.2x Velocidade de movimento");
                addTierLine(lines, currentLevel, 2, "1.3x Velocidade de movimento");
                addTierLine(lines, currentLevel, 3, "1.4x Velocidade de movimento");
            }
            case ENDURANCE -> {
                addTierLine(lines, currentLevel, -3, "7 Corações");
                addTierLine(lines, currentLevel, -2, "8 Corações");
                addTierLine(lines, currentLevel, -1, "9 Corações");
                addTierLine(lines, currentLevel, 0, "10 Corações");
                addTierLine(lines, currentLevel, 1, "11 Corações");
                addTierLine(lines, currentLevel, 2, "12 Corações");
                addTierLine(lines, currentLevel, 3, "13 Corações");
            }
            case WISDOM -> {
                addTierLine(lines, currentLevel, -3, "0.5x XP, -30% duração de poções e encantamentos mais caros");
                addTierLine(lines, currentLevel, -2, "0.7x XP e -15% duração de poções");
                addTierLine(lines, currentLevel, -1, "0.85x Multiplicador de XP");
                addTierLine(lines, currentLevel, 0, "Padrão");
                addTierLine(lines, currentLevel, 1, "1.25x Multiplicador de XP");
                addTierLine(lines, currentLevel, 2, "1.5x XP e +25% duração de poções");
                addTierLine(lines, currentLevel, 3, "1.75x XP, +50% duração de poções e Afinidade de Encantamento");
            }
            case CHARISMA -> {
                addTierLine(lines, currentLevel, -3, "-3 Sorte, +20% azar no /roll e +35% preços com Villagers");
                addTierLine(lines, currentLevel, -2, "-2 Sorte, +10% azar no /roll e +20% preços com Villagers");
                addTierLine(lines, currentLevel, -1, "-1 Sorte e +5% azar no /roll");
                addTierLine(lines, currentLevel, 0, "Padrão");
                addTierLine(lines, currentLevel, 1, "+1 Sorte");
                addTierLine(lines, currentLevel, 2, "+2 Sorte e desconto com Villagers");
                addTierLine(lines, currentLevel, 3, "+3 Sorte, desconto maior e 20% chance de crítico");
            }
        }
        return lines;
    }

    private static void addTierLine(List<Component> lines, int currentLevel, int tier, String text) {
        String prefix = tier > 0 ? "+" + tier : String.valueOf(tier);
        boolean active = (currentLevel == tier);
        ChatFormatting color = active
                ? ChatFormatting.AQUA
                : (tier < 0 ? ChatFormatting.RED : (tier > 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        String marker = active ? "▶ " : "  ";
        lines.add(Component.literal(marker + prefix + ": " + text).withStyle(color));
    }
}
