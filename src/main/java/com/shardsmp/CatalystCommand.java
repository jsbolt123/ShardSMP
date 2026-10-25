package com.shardsmp;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** /catalyst give <player> <type> <tier|shard> [amount] | list | reload */
public final class CatalystCommand implements CommandExecutor, TabCompleter {

    private final ShardSMP plugin;

    public CatalystCommand(ShardSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("shardsmp.admin")) {
            sender.sendMessage(Compat.text("&cYou don't have permission."));
            return true;
        }
        if (args.length == 0) {
            usage(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "give" -> give(sender, args);
            case "list" -> list(sender);
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(Compat.text("&aShardSMP config reloaded."));
            }
            default -> usage(sender);
        }
        return true;
    }

    private void usage(CommandSender s) {
        s.sendMessage(Compat.text("&6/catalyst give <player> <type> <tier|shard> [amount]"));
        s.sendMessage(Compat.text("&6/catalyst list &7- show all elements"));
        s.sendMessage(Compat.text("&6/catalyst reload"));
    }

    private void list(CommandSender s) {
        s.sendMessage(Compat.text("&8&m      &r &6Elements &8&m      "));
        for (Element e : Element.values()) {
            s.sendMessage(Compat.text(e.color + "&l" + e.display + " &7(" + Compat.pretty(e.catalystMaterial.name())
                    + ") &8- &f" + e.ability + "&8: &7" + e.abilityDescription));
        }
    }

    private void give(CommandSender sender, String[] args) {
        if (args.length < 4) {
            usage(sender);
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Compat.text("&cPlayer not found: " + args[1]));
            return;
        }
        Element element = Element.fromString(args[2]);
        if (element == null) {
            sender.sendMessage(Compat.text("&cUnknown element. Use /catalyst list."));
            return;
        }

        int amount = 1;
        if (args.length >= 5) {
            try {
                amount = Math.max(1, Math.min(64, Integer.parseInt(args[4])));
            } catch (NumberFormatException ex) {
                sender.sendMessage(Compat.text("&cAmount must be a number."));
                return;
            }
        }

        ItemStack item;
        String what;
        if (args[3].equalsIgnoreCase("shard")) {
            item = plugin.items().createShard(element, amount);
            what = element.coloredName() + " Shard";
        } else {
            int tier;
            try {
                tier = Integer.parseInt(args[3]);
            } catch (NumberFormatException ex) {
                sender.sendMessage(Compat.text("&cTier must be 1-" + plugin.maxTier() + " or 'shard'."));
                return;
            }
            if (tier < 1 || tier > plugin.maxTier()) {
                sender.sendMessage(Compat.text("&cTier must be 1-" + plugin.maxTier() + " or 'shard'."));
                return;
            }
            item = plugin.items().createCatalyst(element, tier);
            item.setAmount(amount);
            what = element.coloredName() + " Catalyst &7(Tier " + Compat.roman(tier) + ")";
        }

        Map<Integer, ItemStack> leftover = target.getInventory().addItem(item);
        for (ItemStack rest : leftover.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), rest);
        }

        sender.sendMessage(Compat.text("&aGave &f" + amount + "x " + what + " &ato &f" + target.getName() + "&a."));
        if (!sender.equals(target)) {
            target.sendMessage(Compat.text("&7You received &f" + amount + "x " + what + "&7."));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (!sender.hasPermission("shardsmp.admin")) return out;

        if (args.length == 1) {
            for (String s : List.of("give", "list", "reload")) {
                if (s.startsWith(args[0].toLowerCase(Locale.ROOT))) out.add(s);
            }
        } else if (args[0].equalsIgnoreCase("give")) {
            String cur = args[args.length - 1].toLowerCase(Locale.ROOT);
            if (args.length == 2) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase(Locale.ROOT).startsWith(cur)) out.add(p.getName());
                }
            } else if (args.length == 3) {
                for (Element e : Element.values()) {
                    if (e.name().toLowerCase(Locale.ROOT).startsWith(cur)) out.add(e.name().toLowerCase(Locale.ROOT));
                }
            } else if (args.length == 4) {
                List<String> opts = new ArrayList<>();
                for (int i = 1; i <= plugin.maxTier(); i++) opts.add(String.valueOf(i));
                opts.add("shard");
                for (String o : opts) if (o.startsWith(cur)) out.add(o);
            }
        }
        return out;
    }
}
