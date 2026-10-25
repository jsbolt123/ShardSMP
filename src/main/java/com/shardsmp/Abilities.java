package com.shardsmp;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** All active (right-click) abilities. Each returns true only if the ability actually fired. */
public final class Abilities {

    private static final String[] DEBUFFS = {
            "POISON", "WITHER", "SLOWNESS", "WEAKNESS", "BLINDNESS", "HUNGER", "NAUSEA", "MINING_FATIGUE", "LEVITATION"
    };

    private final ShardSMP plugin;

    public Abilities(ShardSMP plugin) {
        this.plugin = plugin;
    }

    public boolean execute(Element e, Player p, int tier) {
        double pw = plugin.power(e, tier);
        return switch (e) {
            case INFERNO -> inferno(p, pw);
            case TEMPEST -> tempest(p, pw);
            case VOID -> voidBlink(p, pw);
            case VERDANT -> verdant(p, pw);
            case FROST -> frost(p, pw);
            case TREMOR -> tremor(p, pw);
            case SOLAR -> solar(p, pw);
            case PHANTOM -> phantom(p, pw);
            case CHRONO -> chrono(p);
        };
    }

    // ------------------------------------------------------------------ INFERNO

    private boolean inferno(Player p, double pw) {
        Vector v = p.getLocation().getDirection().normalize().multiply(1.6 * pw);
        if (v.getY() < 0.25) v.setY(0.25);
        p.setVelocity(v);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 0.8f);

        Particle flame = Compat.particle(Particle.CLOUD, "FLAME");
        new BukkitRunnable() {
            int ticks = 0;
            final Set<UUID> hit = new HashSet<>();

            @Override
            public void run() {
                if (!p.isOnline() || ticks++ > 10) {
                    cancel();
                    return;
                }
                p.setFallDistance(0f);
                p.getWorld().spawnParticle(flame, p.getLocation().add(0, 1, 0), 14, 0.3, 0.3, 0.3, 0.02);
                for (Entity en : p.getNearbyEntities(2, 2, 2)) {
                    if (en instanceof LivingEntity le && hit.add(le.getUniqueId())) {
                        le.setFireTicks(80);
                        le.damage(3.0 * pw, p);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        return true;
    }

    // ------------------------------------------------------------------ TEMPEST

    private boolean tempest(Player p, double pw) {
        Location eye = p.getEyeLocation();
        World w = p.getWorld();
        RayTraceResult r = w.rayTrace(eye, eye.getDirection(), 32.0, FluidCollisionMode.NEVER, true, 0.6,
                en -> en != p && en instanceof LivingEntity);
        if (r == null) {
            p.sendActionBar(Compat.text("&bNothing in range to strike."));
            return false;
        }
        Location target = r.getHitEntity() != null
                ? r.getHitEntity().getLocation()
                : r.getHitPosition().toLocation(w);

        w.strikeLightning(target);
        if (pw >= 1.5) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    w.strikeLightning(target);
                }
            }.runTaskLater(plugin, 8L);
        }
        Compat.give(p, "SPEED", 60, 1);
        return true;
    }

    // ------------------------------------------------------------------ VOID

    private boolean voidBlink(Player p, double pw) {
        Location start = p.getLocation();
        Vector dir = start.getDirection().normalize();
        int max = (int) Math.round(10 * pw);

        for (int d = max; d >= 2; d--) {
            Location c = start.clone().add(dir.clone().multiply(d));
            Block feet = c.getBlock();
            Block head = c.clone().add(0, 1, 0).getBlock();
            if (feet.isPassable() && head.isPassable() && !feet.isLiquid() && !head.isLiquid()) {
                Particle portal = Compat.particle(Particle.CLOUD, "REVERSE_PORTAL", "PORTAL");
                start.getWorld().spawnParticle(portal, start.clone().add(0, 1, 0), 40, 0.4, 0.8, 0.4, 0.2);
                p.teleport(c);
                p.setFallDistance(0f);
                c.getWorld().spawnParticle(portal, c.clone().add(0, 1, 0), 40, 0.4, 0.8, 0.4, 0.2);
                c.getWorld().playSound(c, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.7f);
                return true;
            }
        }
        p.sendActionBar(Compat.text("&5No safe space to blink to."));
        return false;
    }

    // ------------------------------------------------------------------ VERDANT

    private boolean verdant(Player p, double pw) {
        p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + 6.0 * pw));
        Compat.give(p, "REGENERATION", (int) (100 * pw), 1);

        Particle leaf = Compat.particle(Particle.CLOUD, "HAPPY_VILLAGER", "VILLAGER_HAPPY");
        p.getWorld().spawnParticle(leaf, p.getLocation().add(0, 1, 0), 40, 3, 1, 3, 0.0);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1f, 1.4f);

        for (Entity en : p.getNearbyEntities(6, 6, 6)) {
            if (en instanceof Player t && !t.equals(p)) {
                Compat.give(t, "SLOWNESS", (int) (60 * pw), 2);
                Compat.give(t, "WEAKNESS", (int) (60 * pw), 0);
                t.getWorld().spawnParticle(leaf, t.getLocation().add(0, 0.5, 0), 15, 0.4, 0.3, 0.4, 0.0);
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ FROST

    private boolean frost(Player p, double pw) {
        double radius = 6.0 + pw;
        Particle snow = Compat.particle(Particle.CLOUD, "SNOWFLAKE");
        Location c = p.getLocation().add(0, 0.2, 0);

        for (int i = 0; i < 48; i++) {
            double a = Math.PI * 2 * i / 48.0;
            c.getWorld().spawnParticle(snow, c.clone().add(Math.cos(a) * radius, 0.3, Math.sin(a) * radius),
                    4, 0.1, 0.4, 0.1, 0.0);
        }
        c.getWorld().spawnParticle(snow, c.clone().add(0, 1, 0), 80, radius / 2, 0.8, radius / 2, 0.02);
        c.getWorld().playSound(c, Sound.BLOCK_GLASS_BREAK, 1f, 0.6f);

        for (Entity en : p.getNearbyEntities(radius, radius, radius)) {
            if (en instanceof LivingEntity le) {
                Compat.give(le, "SLOWNESS", (int) (80 * pw), 3);
                le.setFreezeTicks(le.getMaxFreezeTicks() + 40);
                le.damage(2.0 * pw, p);
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ TREMOR

    private boolean tremor(Player p, double pw) {
        double radius = 5.0 + pw;
        Location c = p.getLocation();
        Particle boom = Compat.particle(Particle.CLOUD, "EXPLOSION", "EXPLOSION_LARGE");
        c.getWorld().spawnParticle(boom, c.clone().add(0, 0.5, 0), 6, radius / 3, 0.2, radius / 3, 0.0);
        c.getWorld().playSound(c, Sound.ENTITY_GENERIC_EXPLODE, 0.9f, 0.6f);

        for (Entity en : p.getNearbyEntities(radius, 3, radius)) {
            if (en instanceof LivingEntity le) {
                Vector away = le.getLocation().toVector().subtract(c.toVector());
                away.setY(0);
                if (away.lengthSquared() < 0.01) away = new Vector(0, 0, 0);
                else away.normalize().multiply(0.7);
                away.setY(0.9 * Math.min(1.5, pw));
                le.setVelocity(away);
                le.damage(4.0 * pw, p);
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ SOLAR

    private boolean solar(Player p, double pw) {
        for (String debuff : DEBUFFS) {
            var type = Compat.effect(debuff);
            if (type != null) p.removePotionEffect(type);
        }
        p.setFireTicks(0);

        Particle ray = Compat.particle(Particle.CLOUD, "END_ROD", "FLAME");
        p.getWorld().spawnParticle(ray, p.getLocation().add(0, 1, 0), 120, 4, 1.5, 4, 0.05);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);

        for (Entity en : p.getNearbyEntities(8, 8, 8)) {
            if (en instanceof Player t && !t.equals(p)) {
                Compat.give(t, "BLINDNESS", (int) (60 * pw), 0);
            } else if (en instanceof Zombie || en instanceof AbstractSkeleton || en instanceof Phantom) {
                LivingEntity le = (LivingEntity) en;
                le.setFireTicks((int) (100 * pw));
                le.damage(6.0 * pw, p);
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ PHANTOM

    private boolean phantom(Player p, double pw) {
        int ticks = (int) (100 * pw);
        Compat.give(p, "INVISIBILITY", ticks, 0);
        Compat.give(p, "SPEED", ticks, 1);
        plugin.wraiths().add(p.getUniqueId());

        Particle smoke = Compat.particle(Particle.CLOUD, "SMOKE", "SMOKE_NORMAL");
        p.getWorld().spawnParticle(smoke, p.getLocation().add(0, 1, 0), 40, 0.4, 0.8, 0.4, 0.02);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 1f, 0.5f);

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.wraiths().remove(p.getUniqueId());
            }
        }.runTaskLater(plugin, ticks);
        p.sendActionBar(Compat.text("&8Wraith form: attacking will break it."));
        return true;
    }

    /** Called by the listener when a wraith attacks: ends the ghost form early. */
    public void breakWraith(Player p) {
        if (!plugin.wraiths().remove(p.getUniqueId())) return;
        var inv = Compat.effect("INVISIBILITY");
        if (inv != null) p.removePotionEffect(inv);
        p.sendActionBar(Compat.text("&8Your wraith form shatters."));
    }

    // ------------------------------------------------------------------ CHRONO

    private boolean chrono(Player p) {
        Location from = p.getLocation().clone();
        if (!plugin.chrono().rewind(p)) {
            p.sendActionBar(Compat.text("&dNot enough temporal history yet. Hold the Chrono catalyst for a few seconds."));
            return false;
        }
        Particle warp = Compat.particle(Particle.CLOUD, "REVERSE_PORTAL", "PORTAL");
        from.getWorld().spawnParticle(warp, from.add(0, 1, 0), 50, 0.4, 0.8, 0.4, 0.3);
        p.getWorld().spawnParticle(warp, p.getLocation().add(0, 1, 0), 50, 0.4, 0.8, 0.4, 0.3);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1.8f);
        return true;
    }
}
