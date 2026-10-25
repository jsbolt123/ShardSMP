package com.shardsmp;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Records recent position/health snapshots for players holding a Chrono catalyst. */
public final class ChronoTracker {

    private record Snapshot(long time, Location loc, double health) {}

    private final ShardSMP plugin;
    private final Map<UUID, Deque<Snapshot>> history = new HashMap<>();

    public ChronoTracker(ShardSMP plugin) {
        this.plugin = plugin;
    }

    public void record(Player p) {
        long now = System.currentTimeMillis();
        long window = plugin.rewindSeconds() * 1000L + 1500L;
        Deque<Snapshot> dq = history.computeIfAbsent(p.getUniqueId(), k -> new ArrayDeque<>());
        dq.addLast(new Snapshot(now, p.getLocation().clone(), p.getHealth()));
        while (!dq.isEmpty() && dq.peekFirst().time() < now - window) dq.pollFirst();
    }

    /** Teleports the player back to their oldest stored snapshot. Returns false if there is none. */
    public boolean rewind(Player p) {
        Deque<Snapshot> dq = history.get(p.getUniqueId());
        if (dq == null || dq.size() < 2) return false;
        Snapshot s = dq.peekFirst();
        if (s.loc().getWorld() == null) return false;

        p.teleport(s.loc());
        p.setFallDistance(0f);
        p.setFireTicks(0);
        if (s.health() > p.getHealth()) {
            p.setHealth(Math.min(s.health(), p.getMaxHealth()));
        }
        dq.clear();
        return true;
    }

    public void forget(Player p) {
        history.remove(p.getUniqueId());
    }
}
