package com.shardsmp;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/** Repeating task: grants passives for a Catalyst held in the off-hand. */
public final class PassiveTask extends BukkitRunnable {

    private final ShardSMP plugin;

    public PassiveTask(ShardSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        long interval = Math.max(5L, plugin.getConfig().getLong("passive.interval-ticks", 20L));
        ItemFactory items = plugin.items();

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.isDead() || !p.hasPermission("shardsmp.use")) continue;

            ItemStack off = p.getInventory().getItemInOffHand();
            ItemStack main = p.getInventory().getItemInMainHand();

            Element offEl = items.getCatalystElement(off);
            if (offEl != null) {
                int tier = items.getTier(off);
                Compat.give(p, plugin.passiveEffect(offEl), (int) interval + 40, plugin.passiveAmplifier(offEl, tier));
            }

            // Chrono needs history to rewind to; record while it is held in either hand.
            if (offEl == Element.CHRONO || items.getCatalystElement(main) == Element.CHRONO) {
                plugin.chrono().record(p);
            }
        }
    }
}
