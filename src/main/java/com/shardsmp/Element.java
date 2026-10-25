package com.shardsmp;

import org.bukkit.Material;

/**
 * Every elemental Catalyst. Each has a vanilla base item for the full Catalyst,
 * while all shards use PRISMARINE_SHARD and are told apart by PDC only.
 */
public enum Element {
    INFERNO("Inferno", "&c", Material.NETHER_STAR, "Blazing Dash",
            "Dash forward in a burst of flame, igniting foes in your path."),
    TEMPEST("Tempest", "&b", Material.HEART_OF_THE_SEA, "Thunder Lance",
            "Call lightning down on whatever you are looking at."),
    VOID("Void", "&5", Material.ECHO_SHARD, "Void Blink",
            "Slip through the void and reappear ahead of you."),
    VERDANT("Verdant", "&a", Material.EMERALD, "Overgrowth",
            "Heal yourself while thorny roots slow nearby players."),
    FROST("Frost", "&9", Material.BLUE_ICE, "Frost Nova",
            "Flash-freeze everything around you."),
    TREMOR("Tremor", "&6", Material.NETHERITE_SCRAP, "Seismic Slam",
            "Slam the ground and throw nearby enemies into the air."),
    SOLAR("Solar", "&e", Material.SUNFLOWER, "Radiant Pulse",
            "Cleanse your debuffs, blind players and burn the undead."),
    PHANTOM("Phantom", "&8", Material.PHANTOM_MEMBRANE, "Wraith Step",
            "Turn into a ghost: invisible and fast until you strike."),
    CHRONO("Chrono", "&d", Material.CLOCK, "Time Rewind",
            "Snap back to where (and how healthy) you were seconds ago.");

    public final String display;
    public final String color;
    public final Material catalystMaterial;
    public final String ability;
    public final String abilityDescription;

    Element(String display, String color, Material catalystMaterial, String ability, String abilityDescription) {
        this.display = display;
        this.color = color;
        this.catalystMaterial = catalystMaterial;
        this.ability = ability;
        this.abilityDescription = abilityDescription;
    }

    public String coloredName() {
        return color + display;
    }

    public static Element fromString(String s) {
        if (s == null) return null;
        for (Element e : values()) {
            if (e.name().equalsIgnoreCase(s)) return e;
        }
        return null;
    }
}
