# Textures to draw

Every texture below is currently a generated placeholder (`python3 tools/generate_textures.py`).
Replace a PNG with your own art at the same path and size. The generator never overwrites an
existing file unless you pass `--force`.

All paths are under `src/main/resources/assets/olympus/textures/`.

Status: ☐ placeholder · ☑ final art

## Items (16×16)

| Status | File | Notes |
|---|---|---|
| ☐ | `item/olympian_bronze_ingot.png` | bronze-gold ingot |
| ☐ | `item/divine_smithing_template.png` | smithing template, divine / lightning motif |
| ☐ | `item/blessing_core.png` | cyan orb, one star |
| ☐ | `item/ascension_core.png` | gold orb, two stars |
| ☐ | `item/god_swapper.png` | yin-yang style coin of two gods |
| ☐ | `item/gorgoneion_banner_pattern.png` | banner pattern paper |
| ☐ | `item/thunderbolt_banner_pattern.png` | banner pattern paper |
| ☐ | `item/conch_of_the_deep.png` | Poseidon's conch |
| ☐ | `item/helm_of_darkness.png` | Hades' helm (inventory icon) |
| ☐ | `item/owl_of_athena.png` | small owl |
| ☐ | `item/blade_of_ares.png` | sword, drawn handheld (diagonal) |
| ☐ | `item/lyre_of_apollo.png` | golden lyre |
| ☐ | `item/silver_bow.png` | bow, idle |
| ☐ | `item/silver_bow_pulling_0.png` | bow, pull stage 1 |
| ☐ | `item/silver_bow_pulling_1.png` | bow, pull stage 2 |
| ☐ | `item/silver_bow_pulling_2.png` | bow, pull stage 3 |
| ☐ | `item/talaria.png` | winged sandals |
| ☐ | `item/hammer_of_hephaestus.png` | smith's hammer, handheld |
| ☐ | `item/sickle_of_demeter.png` | sickle, handheld |
| ☐ | `item/goblet_of_dionysus.png` | golden goblet with wine |
| ☐ | `item/mirror_of_aphrodite.png` | hand mirror |

The **Aegis** has no item texture: it uses the vanilla shield model, and its look comes from its banner layers (gold base, orange gradient, Gorgoneion, orange border). To change the design, edit the two pattern masks below, or the layers in `ModItems.AEGIS`.

## Blocks (16×16)

| Status | File |
|---|---|
| ☐ | `block/olympian_bronze_block.png` |

## Worn armor (64×32, vanilla humanoid armor layout)

| Status | File | Notes |
|---|---|---|
| ☐ | `entity/equipment/humanoid/helm_of_darkness.png` | only the head area is used |
| ☐ | `entity/equipment/humanoid/talaria.png` | only the feet area is used; wings would need a custom model |

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

## Coming in later phases
Phase 3 craftables (Caduceus, Spear of Athena, Forge Hammer, Harpe, Thyrsus, Cornucopia, Ambrosia, Nectar, Sun Arrows, Hoplite armor), Phase 4 legendaries and the six bosses' recolored placeholder skins. Each phase adds its rows here.
