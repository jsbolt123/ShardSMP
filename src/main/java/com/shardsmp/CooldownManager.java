package com.shardsmp;

import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Per-player, per-element cooldowns stored as expiry timestamps. */
public final class CooldownManager {

    private final Map<UUID, EnumMap<Element, Long>> cooldowns = new HashMap<>();

    /** Remaining cooldown in milliseconds (0 if ready). */
    public long remaining(Player p, Element e) {
        EnumMap<Element, Long> map = cooldowns.get(p.getUniqueId());
        if (map == null) return 0L;
        Long expiry = map.get(e);
        if (expiry == null) return 0L;
        long left = expiry - System.currentTimeMillis();
        if (left <= 0) {
            map.remove(e);
            return 0L;
        }
        return left;
    }

    public void set(Player p, Element e, int seconds) {
        cooldowns.computeIfAbsent(p.getUniqueId(), k -> new EnumMap<>(Element.class))
                .put(e, System.currentTimeMillis() + seconds * 1000L);
    }

    public void clear(Player p) {
        cooldowns.remove(p.getUniqueId());
    }
}
