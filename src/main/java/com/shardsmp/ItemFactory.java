package com.shardsmp;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/** Builds and identifies Catalysts and Shards. Identity lives only in the item's PDC. */
public final class ItemFactory {

    private final ShardSMP plugin;
    private final NamespacedKey catalystKey;
    private final NamespacedKey tierKey;
    private final NamespacedKey shardKey;

    public ItemFactory(ShardSMP plugin) {
        this.plugin = plugin;
        this.catalystKey = new NamespacedKey(plugin, "catalyst_type");
        this.tierKey = new NamespacedKey(plugin, "catalyst_tier");
        this.shardKey = new NamespacedKey(plugin, "shard_type");
    }

    // ---------- creation ----------

    public ItemStack createCatalyst(Element e, int tier) {
        ItemStack item = new ItemStack(e.catalystMaterial);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Compat.text(e.color + "&l" + e.display + " Catalyst &7[Tier " + Compat.roman(tier) + "]"));

        String effectName = plugin.passiveEffect(e);
        int amp = plugin.passiveAmplifier(e, tier);

        List<Component> lore = new ArrayList<>();
        lore.add(Compat.text("&8Elemental Catalyst"));
        lore.add(Component.empty());
        lore.add(Compat.text("&7Off-hand: " + e.color + Compat.pretty(effectName) + " " + Compat.roman(amp + 1)));
        lore.add(Compat.text("&7Right-click: " + e.color + e.ability));
        lore.add(Compat.text("&8" + e.abilityDescription));
        lore.add(Component.empty());
        lore.add(Compat.text("&7Cooldown: &f" + plugin.cooldownSeconds(e, tier) + "s"));
        lore.add(Compat.text("&7Power: &f" + String.format("%.0f%%", plugin.power(e, tier) * 100)));
        meta.lore(lore);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(catalystKey, PersistentDataType.STRING, e.name());
        pdc.set(tierKey, PersistentDataType.INTEGER, tier);

        glow(meta);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createShard(Element e, int amount) {
        ItemStack item = new ItemStack(Material.PRISMARINE_SHARD, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Compat.text(e.color + e.display + " Shard"));

        List<Component> lore = new ArrayList<>();
        lore.add(Compat.text("&8Catalyst Shard"));
        lore.add(Component.empty());
        lore.add(Compat.text("&7A fragment of the " + e.color + e.display + " &7Catalyst."));
        lore.add(Compat.text("&7Left behind when a bearer falls."));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(shardKey, PersistentDataType.STRING, e.name());

        glow(meta);
        item.setItemMeta(meta);
        return item;
    }

    private void glow(ItemMeta meta) {
        Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
        if (ench != null) {
            meta.addEnchant(ench, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
    }

    // ---------- identification ----------

    /** Element of a full Catalyst, or null if the item is not one. */
    public Element getCatalystElement(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String s = item.getItemMeta().getPersistentDataContainer().get(catalystKey, PersistentDataType.STRING);
        return Element.fromString(s);
    }

    public int getTier(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 1;
        Integer t = item.getItemMeta().getPersistentDataContainer().get(tierKey, PersistentDataType.INTEGER);
        return t == null ? 1 : Math.max(1, t);
    }

    /** Element of a Shard, or null if the item is not one. */
    public Element getShardElement(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String s = item.getItemMeta().getPersistentDataContainer().get(shardKey, PersistentDataType.STRING);
        return Element.fromString(s);
    }
}
