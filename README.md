# Relics of Olympus

A Fabric mod for **Minecraft 1.21.11** (Java 21). The gods of Olympus claim every player, hand out relics with real powers, and summon boss events whose legendary rewards exist only once per server. Built for PvP survival servers.

**Install:** Fabric Loader 0.20.1+, Fabric API 0.141.6+1.21.11, and this jar in `mods/`, on the server **and** every client.

## How it plays
1. **Claimed on first join.** A random god claims you ("You have been claimed by Zeus!") and gives you their starter relic.
2. **Carry at most 3 god items.** They drop when you die (others can take them), never break, glow on the ground, never despawn, and survive fire, lava and explosions (the void still destroys them). No ender chests, shulker boxes or bundles.
3. **Upgrade** in a smithing table: Blessing Core → **Blessed ★** (-25% cooldowns, unlocks the Divine Power on **R**), then Ascension Core → **Ascended ★★** (-50% cooldowns, +25% durations and radii, gold name).
4. **Reroll** your god with the God Swapper: hold it, starter relic in offhand, hold right-click 3 s.
5. **Craft** stronger relics: each one consumes its god's starter relic.
6. **Fight boss events** for legendaries. The top damage dealer wins; everyone with 10%+ damage gets bronze and maybe a template.

Every relic looks like its vanilla base item (Talaria = gold leather boots, Goblet = potion...). Read the in-game **Codex of Olympus** (book + gold ingot) for everything.

## Starter relics
| Item | Primary power | Blessed ★ ability (key R) |
|---|---|---|
| **Aegis** | Blocked melee hits push the attacker back, with a (see config) chance to strike them with lightning. | **Divine Shockwave**: Every (see config)th blocked hit releases a shockwave that knocks back and smites hostile mobs within (see config) blocks. |
| **Conch of the Deep** | Tidal Surge: a wave crashes (see config) blocks ahead, knocking enemies back and slowing them for (see config)s. You gain Water Breathing and Dolphin's Grace. | **Riptide Dash**: Launch forward like Riptide, even on dry land. |
| **Helm of Darkness** | Sneak in place for (see config)s to vanish completely, armor and all, with Speed I, for (see config)s. | **Ambush**: While invisible, your first hit deals (see config)x damage and ends the invisibility. |
| **Owl of Athena** | Reveal: everything within (see config) blocks glows for (see config)s and takes (see config) more damage from you. | **Battle Wisdom**: You and players within (see config) blocks gain Strength I and Resistance I for (see config)s. |
| **Blade of Ares** | Fury: (see config) hits in a row grant Strength I. Kills grant Strength II for (see config)s. | **War Cry**: Mobs within (see config) blocks are weakened; you and nearby players gain Speed II for (see config)s. |
| **Lyre of Apollo** | Right-click: you and allies within (see config) blocks get Regeneration II and Speed I for (see config)s, and are cleansed of Poison and Wither. | **Sunburst**: Sets hostile mobs within (see config) blocks ablaze and blinds enemies for (see config)s. |
| **Silver Bow** | Arrows fly perfectly straight, deal (see config) more damage, and anything they hit glows for (see config)s. | **Volley**: Your next shot fires (see config) arrows in a spread for the cost of one (within (see config)s). |
| **Talaria** | Permanent Speed I and a double jump while worn. | **Wings of Hermes**: Triple jump, and you never take fall damage. |
| **Hammer of Hephaestus** | Hits set enemies ablaze for (see config)s, you're immune to fire while holding it, and mined ores come out smelted. | **Forge Fire**: Instantly smelts up to (see config) items from the stack in your offhand. |
| **Sickle of Demeter** | Famine: hits cause Hunger and Slowness and drain (see config) food from players. Right-click crops to ripen a (see config)x(see config) area. | **Great Harvest**: Harvests and replants every ripe crop in a (see config)x(see config) area. |
| **Goblet of Dionysus** | Drink: a random level II combat blessing for (see config)s ((see config) chance of a hangover instead). Never empties. | **Wild Party**: Players within (see config) blocks get a random blessing for (see config)s; mobs stagger drunkenly. |
| **Mirror of Aphrodite** | Enchanting Gaze: the player you look at can't hurt you for (see config)s and is slowed. Nearby hostile mobs also leave you alone. | **Charm**: The mob you look at fights for you for (see config)s. Bosses and players resist. |

## Craftable god items
| Item | Primary power | Blessed ★ ability (key R) |
|---|---|---|
| **Caduceus** | Right-click: blink up to (see config) blocks where you look (never through walls). | **Waypoint**: First press marks your spot; press again within (see config) minutes to return to it. |
| **Spear of Athena** | A thrown spear that pierces up to (see config) enemies and always returns to you. | **Aegis Strike**: Your next throw lands with a shockwave that knocks back and slows enemies within (see config) blocks. |
| **Forge Hammer** | Mines a 3x3 area (sneak to mine a single block). | **Forge Mode**: Switch between 3x3 and 5x5 mining. |
| **Harpe** | Perseus' blade: (see config) more damage against bosses. | **Hero's Leap**: Leap at the creature you look at (up to (see config) blocks) and strike it for double damage. |
| **Thyrsus** | Hits make enemies stagger: Slowness II and dizziness for (see config)s. | **Vine Snare**: Roots every enemy within (see config) blocks in place for (see config)s. |
| **Cornucopia** | Right-click: fully restores your hunger and saturation. | **Feast**: Fully feeds every player within (see config) blocks and grants Regeneration. |

## Legendaries (boss rewards, one of each per server)
| Item | Primary power | Blessed ★ ability (key R) |
|---|---|---|
| **Thunderbolt of Zeus** | Throw it: (see config) lightning strikes rain around the impact, then it returns to your hand. | **Skybreaker**: Launch about (see config) blocks into the sky and land in a ring of lightning. No fall damage. |
| **Trident of Poseidon** | Release to Riptide anywhere, even on dry land, with a wave that throws enemies back. Sneak to throw: Channeling is always on. | **Earthshaker**: Every enemy within (see config) blocks is hurled into the air. |
| **Bident of Hades** | Right-click: (see config) skeleton warriors rise to fight for you for (see config)s. | **Soul Harvest**: For (see config)s, everything you kill rises as a skeleton ally. |
| **Head of Medusa** | Look at a creature and right-click: it turns to stone for (see config)s (shorter on players). Wear it on your head. | **Gorgon's Gaze**: Petrifies everything in a (see config)-block cone in front of you for (see config)s. |
| **Golden Fleece** | While worn: constant Regeneration and immunity to fire. | **Undying**: Once every (see config) minutes, a killing blow leaves you standing instead. |
| **Scythe of Kronos** | Right-click: time freezes for everything within (see config) blocks (Slowness IV, Mining Fatigue III) for (see config)s. | **Rewind**: Return to where you were (see config) seconds ago, with the health you had then. |

## Other items
| Item | What it does |
|---|---|
| Olympian Bronze Ingot / Block | Base material |
| Divine Smithing Template | Smithing template, ~8% in Ancient City / Bastion / End City treasure; copyable |
| Blessing Core / Ascension Core | Upgrade cores (smithing table template slot) |
| God Swapper | Rerolls your starter relic |
| Ambrosia | Heals 6 hearts, Regeneration II, Absorption II (30 s cooldown) |
| Nectar | Clears all harmful effects, Resistance I |
| Sun Arrows | Set targets on fire and light up where they land |
| Hoplite Armor | Full set: Resistance I + extra knockback resistance (normal durability) |
| Gorgoneion / Thunderbolt Banner Patterns | Banner and shield designs for the loom |
| Codex of Olympus | The in-game guide |

## Recipes
See **[RECIPES.md](RECIPES.md)**. All recipes are datapack JSON (`data/olympus/recipe/`), so a datapack can change any of them.

## Events
| Event | Boss | Where | Legendary |
|---|---|---|---|
| Wrath of Zeus | Typhon | anywhere (also starts by itself in thunderstorms) | Thunderbolt of Zeus |
| Rise of the Deep | Cetus | deep ocean only | Trident of Poseidon |
| Gates of the Underworld | Cerberus | Nether only | Bident of Hades |
| Gorgon's Lair | Medusa (look away when she glares!) | anywhere | Head of Medusa |
| Quest for the Fleece | Colchian Dragon | anywhere | Golden Fleece |
| Titanomachy | Kronos | anywhere | Scythe of Kronos (+ Ascension Core) |

Bosses have boss bars, scale their health with nearby players, telegraph every attack, never break blocks, and teleport to players who try to cheese them. Kronos also drops an Ascension Core for the top fighter.

## Commands (op level 2)
| Command | |
|---|---|
| `/olympus claim <players> <god>` | Claim players for a god (gives the starter relic) |
| `/olympus give <players> <item> [0-2]` | Give any Olympus item at a level |
| `/olympus setlevel <0-2>` | Set the level of the god item in your hand |
| `/olympus event start <event>` | Start an event in front of you |
| `/olympus event stop` | End the running event |
| `/olympus legendaries` | Who has each legendary |
| `/olympus legendaries reset <id>` | Let a lost legendary drop again |
| `/olympus reload` | Reload `config/olympus.json` |

## Controls
- **R**: Divine Power (Blessed ability). Rebind under Controls → Relics of Olympus. A HUD icon next to the hotbar shows its cooldown.
- **Jump in mid-air** with Talaria for the double jump (Survival).

## Config
`config/olympus.json` holds every number (damage, durations, cooldowns, radii, chances, item limit, loot chances, boss health, rewards). It's created with defaults on first run and synced to clients; `/olympus reload` applies changes live. Notable options: `general.maxGodItems` (3), `general.starterTier` (netherite / diamond / iron, restart needed), `general.abilitiesAffectPlayers` (turn PvP effects off), `general.claimDelayMinutes` (anti alt-farming), `lightning.lightningFire` (off: no griefing), `events.wrathAutoStartChance`.

## Development
- `./gradlew build`: builds and runs 27 automated in-game tests
- `xvfb-run ./gradlew runClientGameTest`: plays every relic and boss in a real client
- `./gradlew runDatagen`: regenerates recipes, models, tags, loot, lang
- Bosses use tinted vanilla models; swap in Blockbench models in `BossRenderers`. See TEXTURES_TODO.md.
