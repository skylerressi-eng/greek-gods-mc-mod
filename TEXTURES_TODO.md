# Textures to draw

Every texture below is currently a generated placeholder (`python3 tools/generate_textures.py`).
Replace a PNG with your own art at the same path and size. The generator never overwrites an
existing file unless you pass `--force`.

All paths are under `src/main/resources/assets/olympus/textures/`.

Status: ☐ placeholder · ☑ final art

## Items
All items now use **vanilla Minecraft textures**: each relic looks like its base item (Talaria = gold-dyed leather boots, Goblet = a wine-red potion, Blade of Ares = netherite sword, and so on). The models are in `OlympusModelProvider`. To give an item custom art later, draw it at `item/<name>.png` and point its model at it there (`python3 tools/generate_textures.py --items` writes placeholder art to start from).

## Blocks (16×16)

| Status | File |
|---|---|
| ☐ | `block/olympian_bronze_block.png` |

## Banner & shield patterns (64×64 white masks; the dye color tints them)

| Status | File | Notes |
|---|---|---|
| ☐ | `entity/banner/gorgoneion.png` | Medusa's head; front face 20×40 at (1,1), back face at (22,1) |
| ☐ | `entity/shield/gorgoneion.png` | shield face 10×20 at (2,2) |
| ☐ | `entity/banner/thunderbolt.png` | Zeus' bolt |
| ☐ | `entity/shield/thunderbolt.png` | |

## Particles (8×8; more frames can be added in `particles/<name>.json`)

| Status | File | God |
|---|---|---|
| ☐ | `particle/divine_glint.png` | generic gold sparkle |
| ☐ | `particle/storm_spark.png` | Zeus |
| ☐ | `particle/tide_drop.png` | Poseidon |
| ☐ | `particle/shade.png` | Hades |
| ☐ | `particle/owl_feather.png` | Athena |
| ☐ | `particle/war_ember.png` | Ares |
| ☐ | `particle/sun_mote.png` | Apollo |
| ☐ | `particle/moonbeam.png` | Artemis |
| ☐ | `particle/wind_wisp.png` | Hermes |
| ☐ | `particle/forge_spark.png` | Hephaestus |
| ☐ | `particle/grain_leaf.png` | Demeter |
| ☐ | `particle/wine_drop.png` | Dionysus |
| ☐ | `particle/rose_petal.png` | Aphrodite |
| ☐ | `particle/titan_dust.png` | Titans (Kronos) |

## Mod icon

| Status | File |
|---|---|
| ☐ | `assets/olympus/icon.png` (128×128; currently the Fabric template icon) |

## Bosses
The six bosses use **tinted vanilla models** (Typhon = blaze, Cetus = elder guardian, Cerberus = hoglin, Medusa = witch, Colchian Dragon = zoglin, Kronos = wither skeleton). Replace each in `client/render/BossRenderers.java` with a Blockbench model when ready.

## Older note
Phase 3 craftables (Caduceus, Spear of Athena, Forge Hammer, Harpe, Thyrsus, Cornucopia, Ambrosia, Nectar, Sun Arrows, Hoplite armor), Phase 4 legendaries and the six bosses' recolored placeholder skins. Each phase adds its rows here.
