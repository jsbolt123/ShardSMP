package com.shardsmp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.LivingEntity;

import java.util.Locale;
import java.util.Map;

/** Small helpers so effect/particle names work across 1.20.x and 1.21.x. */
public final class Compat {

    private Compat() {}

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("RESISTANCE", "DAMAGE_RESISTANCE"),
            Map.entry("HASTE", "FAST_DIGGING"),
            Map.entry("JUMP_BOOST", "JUMP"),
            Map.entry("SLOWNESS", "SLOW"),
            Map.entry("MINING_FATIGUE", "SLOW_DIGGING"),
            Map.entry("NAUSEA", "CONFUSION"),
            Map.entry("STRENGTH", "INCREASE_DAMAGE"),
            Map.entry("INSTANT_HEALTH", "HEAL"),
            Map.entry("INSTANT_DAMAGE", "HARM")
    );

    public static PotionEffectType effect(String name) {
        if (name == null) return null;
        String n = name.toUpperCase(Locale.ROOT);
        PotionEffectType t = PotionEffectType.getByName(n);
        if (t == null && ALIASES.containsKey(n)) t = PotionEffectType.getByName(ALIASES.get(n));
        if (t == null) {
            for (Map.Entry<String, String> en : ALIASES.entrySet()) {
                if (en.getValue().equals(n)) {
                    t = PotionEffectType.getByName(en.getKey());
                    if (t != null) break;
                }
            }
        }
        return t;
    }

    /** Applies an effect by name; returns false if the name is unknown on this server version. */
    public static boolean give(LivingEntity target, String name, int ticks, int amplifier) {
        PotionEffectType type = effect(name);
        if (type == null) return false;
        target.addPotionEffect(new PotionEffect(type, Math.max(1, ticks), Math.max(0, amplifier), true, false, true));
        return true;
    }

    public static Particle particle(Particle fallback, String... names) {
        for (String n : names) {
            try {
                return Particle.valueOf(n);
            } catch (IllegalArgumentException ignored) {
                // try next name
            }
        }
        return fallback;
    }

    /** "FIRE_RESISTANCE" -> "Fire Resistance" */
    public static String pretty(String raw) {
        StringBuilder sb = new StringBuilder();
        for (String part : raw.toLowerCase(Locale.ROOT).split("_")) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    public static String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(n);
        };
    }

    /** Legacy &-coded text to a non-italic Component. */
    public static Component text(String legacy) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(legacy)
                .decoration(TextDecoration.ITALIC, false);
    }
}
