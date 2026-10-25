package com.shardsmp;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public final class AbilityListener implements Listener {

    private final ShardSMP plugin;

    public AbilityListener(ShardSMP plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------------- right-click abilities

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        Element element = plugin.items().getCatalystElement(item);
        if (element == null) return;

        // Catalysts are never placed/used as their vanilla item.
        event.setCancelled(true);

        Player p = event.getPlayer();
        if (!p.hasPermission("shardsmp.use")) return;

        long remaining = plugin.cooldowns().remaining(p, element);
        if (remaining > 0) {
            p.sendActionBar(Compat.text(element.color + "&l" + element.ability
                    + " &7on cooldown: &f" + format(remaining) + "s"));
            return;
        }

        int tier = plugin.items().getTier(item);
        if (!plugin.abilities().execute(element, p, tier)) return;

        int cd = plugin.cooldownSeconds(element, tier);
        plugin.cooldowns().set(p, element, cd);
        p.sendActionBar(Compat.text(element.color + "&l" + element.ability
                + " &7used! Cooldown: &f" + cd + "s"));
    }

    private static String format(long millis) {
        return String.format("%.1f", millis / 1000.0);
    }

    // ---------------------------------------------------------- phantom: attacking breaks ghost form

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker && plugin.wraiths().contains(attacker.getUniqueId())) {
            plugin.abilities().breakWraith(attacker);
        }
    }

    // ---------------------------------------------------------- death: leave shards behind

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        boolean keep = event.getKeepInventory();
        if (keep && !plugin.getConfig().getBoolean("death.drop-shard-with-keep-inventory", false)) return;

        Set<Element> found = EnumSet.noneOf(Element.class);
        ItemStack[] contents = victim.getInventory().getContents();
        for (ItemStack stack : contents) {
            Element e = plugin.items().getCatalystElement(stack);
            if (e != null) found.add(e);
        }
        if (found.isEmpty()) return;

        if (plugin.getConfig().getBoolean("death.destroy-catalysts", true)) {
            if (keep) {
                for (int i = 0; i < contents.length; i++) {
                    if (plugin.items().getCatalystElement(contents[i]) != null) {
                        victim.getInventory().setItem(i, null);
                    }
                }
            } else {
                event.getDrops().removeIf(s -> plugin.items().getCatalystElement(s) != null);
            }
        }

        for (Element e : found) {
            ItemStack shard = plugin.items().createShard(e, 1);
            if (keep) {
                victim.getWorld().dropItemNaturally(victim.getLocation(), shard);
            } else {
                event.getDrops().add(shard);
            }
        }
    }

    // ---------------------------------------------------------- cleanup

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.chrono().forget(event.getPlayer());
        plugin.wraiths().remove(event.getPlayer().getUniqueId());
    }
}
