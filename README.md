# ShardSMP

Elemental Catalysts and Prismarine Shards for Paper 1.20+ (Java 17+).

## Build
```
mvn clean package
```
The plugin jar is created at `target/ShardSMP-1.0.0.jar`. Drop it in your server's `plugins/` folder.

## Elements

| Element | Catalyst item | Off-hand passive | Right-click ability |
|---|---|---|---|
| Inferno | Nether Star | Fire Resistance | Blazing Dash: dash forward igniting and damaging foes |
| Tempest | Heart of the Sea | Speed I | Thunder Lance: lightning on your target (+ short Speed II) |
| Void | Echo Shard | Slow Falling | Void Blink: teleport forward through walls to a safe spot |
| Verdant | Emerald | Regeneration | Overgrowth: heal yourself, root and weaken nearby players |
| Frost | Blue Ice | Resistance | Frost Nova: freeze, slow and chill everything around you |
| Tremor | Netherite Scrap | Haste II | Seismic Slam: shockwave that launches nearby entities |
| Solar | Sunflower | Night Vision | Radiant Pulse: cleanse your debuffs, blind players, burn undead |
| Phantom | Phantom Membrane | Jump Boost II | Wraith Step: invisible + fast until you attack |
| Chrono | Clock | Luck | Time Rewind: return to your position/health from ~5 seconds ago |

All shards are `PRISMARINE_SHARD`; the element is stored only in the item's PDC (`shard_type`).
Catalysts store `catalyst_type` and `catalyst_tier` in PDC.

## Tiers
Tier 1-3. Higher tiers scale ability power (+25% per tier) and cut cooldown (-10% per tier); the top tier
also gets +1 passive level. All of it is configurable in `config.yml`.

## Death
If a dying player carries a Catalyst anywhere in their inventory (hotbar, off-hand, armor slots), it is
replaced by one shard of that element in their drops. Toggle with `death.destroy-catalysts`.

## Command (permission `shardsmp.admin`, default op)
```
/catalyst give <player> <type> <tier|shard> [amount]
/catalyst list
/catalyst reload
```
Examples: `/catalyst give Steve inferno 2`, `/catalyst give Steve chrono shard 3`
