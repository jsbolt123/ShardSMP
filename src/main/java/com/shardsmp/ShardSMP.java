package com.shardsmp;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ShardSMP extends JavaPlugin {

    private ItemFactory items;
    private CooldownManager cooldowns;
    private Abilities abilities;
    private ChronoTracker chrono;
    private final Set<UUID> wraiths = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.items = new ItemFactory(this);
        this.cooldowns = new CooldownManager();
        this.chrono = new ChronoTracker(this);
        this.abilities = new Abilities(this);

        getServer().getPluginManager().registerEvents(new AbilityListener(this), this);

        CatalystCommand cmd = new CatalystCommand(this);
        if (getCommand("catalyst") != null) {
            getCommand("catalyst").setExecutor(cmd);
            getCommand("catalyst").setTabCompleter(cmd);
        }

        long interval = Math.max(5L, getConfig().getLong("passive.interval-ticks", 20L));
        new PassiveTask(this).runTaskTimer(this, 40L, interval);

        getLogger().info("ShardSMP enabled with " + Element.values().length + " elements.");
    }

    // ---------- accessors ----------

    public ItemFactory items() { return items; }
    public CooldownManager cooldowns() { return cooldowns; }
    public Abilities abilities() { return abilities; }
    public ChronoTracker chrono() { return chrono; }
    public Set<UUID> wraiths() { return wraiths; }

    // ---------- config helpers ----------

    public int maxTier() {
        return Math.max(1, getConfig().getInt("tiers.max-tier", 3));
    }

    public int cooldownSeconds(Element e, int tier) {
        int base = getConfig().getInt("elements." + e.name() + ".cooldown", 30);
        double reduction = getConfig().getDouble("tiers.cooldown-reduction-per-tier", 0.10);
        return (int) Math.max(1, Math.round(base * (1.0 - reduction * (tier - 1))));
    }

    public double power(Element e, int tier) {
        double base = getConfig().getDouble("elements." + e.name() + ".power", 1.0);
        double bonus = getConfig().getDouble("tiers.power-bonus-per-tier", 0.25);
        return base * (1.0 + bonus * (tier - 1));
    }

    public String passiveEffect(Element e) {
        return getConfig().getString("elements." + e.name() + ".passive-effect", "SPEED");
    }

    public int passiveAmplifier(Element e, int tier) {
        int amp = getConfig().getInt("elements." + e.name() + ".passive-amplifier", 0);
        if (tier >= maxTier() && tier > 1 && getConfig().getBoolean("tiers.top-tier-passive-bonus", true)) amp++;
        return amp;
    }

    public int rewindSeconds() {
        return Math.max(1, getConfig().getInt("elements.CHRONO.rewind-seconds", 5));
    }
}
