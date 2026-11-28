# Relics of Olympus

A Fabric mod for **Minecraft 1.21.11** (Java 21). The gods of Olympus claim every player, hand out relics with real powers, and summon boss events whose legendary rewards exist only once per server. Built for PvP survival servers.

**Install:** Fabric Loader 0.20.1+, Fabric API 0.141.6+1.21.11, and this jar in `mods/`, on the server **and** every client.

## How it plays
1. **Claimed on first join.** A random god claims you ("You have been claimed by Zeus!") and gives you their starter relic.
2. **Carry at most 3 god items** (Laurels of Olympus raise it to 5). They only leave you when you die (others can take them) or through `/trade`, never break, glow on the ground, never despawn, and survive fire, lava and explosions (the void still destroys them). They can't be thrown away (Q or dragging out of the inventory), renamed, put in item frames or armor stands, or stored: no chests, barrels, ender chests, shulker boxes, hoppers, furnaces, donkey/boat chests, pots, shelves or bundles (crafting, smithing and anvils still work).
3. **Upgrade** in a smithing table: Blessing Core → **Blessed ★** (-25% cooldowns, unlocks the Divine Power on **R**), then Ascension Core → **Ascended ★★** (-50% cooldowns, +25% durations and radii, gold name).
4. **Reroll** your god with the God Swapper: hold it, starter relic in offhand, hold right-click 3 s.
5. **Craft** stronger relics: each one consumes its god's starter relic.
6. **Fight boss events** for legendaries. The top damage dealer wins; everyone with 10%+ damage gets bronze and maybe a template.

Every relic looks like its vanilla base item (Talaria = gold leather boots, Goblet = potion...). Read the in-game **Codex of Olympus** (book + gold ingot) for everything.

## Starter relics
| Item | Primary power | Blessed ★ ability (key R) |
|---|---|---|
| **Aegis** | Blocked melee hits push the attacker back, with a 30% chance to strike them with lightning. | **Divine Shockwave**: Every 5th blocked hit releases a shockwave that knocks back and smites hostile mobs within 5 blocks. |
| **Conch of the Deep** | Tidal Surge: a wave crashes 7 blocks ahead, knocking enemies back and slowing them for 3s. You gain Water Breathing and Dolphin's Grace. | **Riptide Dash**: Launch forward like Riptide, even on dry land. |
| **Helm of Darkness** | Sneak in place for 2s to vanish completely, armor and all, with Speed I, for 8s. | **Ambush**: While invisible, your first hit deals 2x damage and ends the invisibility. |
| **Owl of Athena** | Reveal: everything within 30 blocks glows for 10s and takes 15% more damage from you. | **Battle Wisdom**: You and allies within 10 blocks gain Strength I and Resistance I for 10s. |
| **Blade of Ares** | Fury: 3 hits in a row grant Strength I. Kills grant Strength II for 8s. | **War Cry**: Mobs within 8 blocks are weakened; you and nearby allies gain Speed II for 10s. |
| **Lyre of Apollo** | Right-click: you and allies within 8 blocks get Regeneration II and Speed I for 5s, and are cleansed of Poison and Wither. | **Sunburst**: Sets hostile mobs within 8 blocks ablaze and blinds enemies for 3s. |
| **Silver Bow** | Arrows fly perfectly straight, deal 15% more damage, and anything they hit glows for 5s. | **Volley**: Your next shot fires 5 arrows in a spread for the cost of one (within 10s). |
| **Talaria** | Permanent Speed I and a double jump while worn. | **Wings of Hermes**: Triple jump, and you never take fall damage. |
| **Hammer of Hephaestus** | Hits set enemies ablaze for 4s, you're immune to fire while holding it, and mined ores come out smelted. | **Forge Fire**: Instantly smelts up to 16 items from the stack in your offhand. |
| **Sickle of Demeter** | Famine: hits cause Hunger and Slowness and drain 2 food from players. Right-click crops to ripen a 5x5 area. | **Great Harvest**: Harvests and replants every ripe crop in a 9x9 area. |
| **Goblet of Dionysus** | Drink: a random level II combat blessing for 30s (10% chance of a hangover instead). Never empties. | **Wild Party**: Allies within 8 blocks get a random blessing for 30s; enemies stagger drunkenly. |
| **Mirror of Aphrodite** | Enchanting Gaze: the player you look at can't hurt you for 3s and is slowed. Nearby hostile mobs also leave you alone. | **Charm**: The mob you look at fights for you for 15s. Bosses and players resist. |

## Craftable god items
| Item | Primary power | Blessed ★ ability (key R) |
|---|---|---|
| **Caduceus** | Right-click: blink up to 10 blocks where you look (never through walls). | **Waypoint**: First press marks your spot; press again within 5 minutes to return to it. |
| **Spear of Athena** | A thrown spear that pierces up to 3 enemies and always returns to you. | **Aegis Strike**: Your next throw lands with a shockwave that knocks back and slows enemies within 4 blocks. |
| **Forge Hammer** | Mines a 3x3 area (sneak to mine a single block). | **Forge Mode**: Switch between 3x3 and 5x5 mining. |
| **Harpe** | Perseus' blade: 50% more damage against bosses. | **Hero's Leap**: Leap at the creature you look at (up to 12 blocks) and strike it for double damage. |
| **Thyrsus** | Hits make enemies stagger: Slowness II and dizziness for 5s. | **Vine Snare**: Roots every enemy within 6 blocks in place for 3s. |
| **Cornucopia** | Right-click: fully restores your hunger and saturation. | **Feast**: Fully feeds every ally within 10 blocks and grants Regeneration. |

## Legendaries (boss rewards, one of each per server)
| Item | Primary power | Blessed ★ ability (key R) |
|---|---|---|
| **Thunderbolt of Zeus** | Throw it: 5 lightning strikes rain around the impact, then it returns to your hand. | **Skybreaker**: Launch about 15 blocks into the sky and land in a ring of lightning. No fall damage. |
| **Trident of Poseidon** | Release to Riptide anywhere, even on dry land, with a wave that throws enemies back. Sneak to throw: Channeling is always on. | **Earthshaker**: Every enemy within 10 blocks is hurled into the air. |
| **Bident of Hades** | Right-click: 3 skeleton warriors rise to fight for you for 30s. | **Soul Harvest**: For 15s, everything you kill rises as a skeleton ally. |
| **Head of Medusa** | Look at a creature and right-click: it turns to stone for 4s (shorter on players). Wear it on your head. | **Gorgon's Gaze**: Petrifies everything in a 10-block cone in front of you for 3s. |
| **Golden Fleece** | While worn: constant Regeneration and immunity to fire. | **Undying**: Once every 10 minutes, a killing blow leaves you standing instead. |
| **Scythe of Kronos** | Right-click: time freezes for everything within 15 blocks (Slowness IV, Mining Fatigue III) for 6s. | **Rewind**: Return to where you were 5 seconds ago, with the health you had then. |

## Other items
| Item | What it does |
|---|---|
| Olympian Bronze Ingot / Block | Base material |
| Divine Smithing Template | Smithing template, ~8% in Ancient City / Bastion / End City treasure; copyable |
| Blessing Core / Ascension Core | Upgrade cores (smithing table template slot) |
| God Swapper | Rerolls your starter relic |
| Laurel of Olympus | Right-click: permanently carry one more god item (up to 5) |
| Ambrosia | Full heal, clears harmful effects, Regeneration III, Absorption IV, Resistance II, Strength, Fire Resistance, full hunger (30 s cooldown) |
| Nectar | Clears all harmful effects, Resistance I |
| Sun Arrows | Set targets on fire and light up where they land |
| Hoplite Armor | Netherite armor with a bonus per piece (helmet: no Blindness/Darkness, breastplate: +2 hearts, greaves: +10% speed, sandals: half fall damage). Full set: Resistance I below half health, extra knockback resistance, melee attackers take 15% back |
| Gorgoneion / Thunderbolt Banner Patterns | Banner and shield designs for the loom |
| Codex of Olympus | The in-game guide |

## Recipes
See **[RECIPES.md](RECIPES.md)**. All recipes are datapack JSON (`data/olympus/recipe/`), so a datapack can change any of them.

## Events
| Event | Boss | Where | Attacks | Rage ultimate (below 50%) | Legendary |
|---|---|---|---|---|---|
| Wrath of Zeus | Typhon | anywhere (also starts by itself in thunderstorms) | Lightning Ring, Storm Pull, Thunder Slam, Chain Lightning | **Eye of the Storm**: rings of lightning roll outward | Thunderbolt of Zeus |
| Rise of the Deep | Cetus | deep ocean only | Tidal Charge, Whirlpool, Call of the Drowned, Tidal Wave | **Maelstrom**: a whirlpool drags everyone in for 4s | Trident of Poseidon |
| Gates of the Underworld | Cerberus | Nether only | Hellfire Breath, Triple Bite, Howl, Pounce | **Three Heads of Hellfire**: three fire streams spin around him | Bident of Hades |
| Gorgon's Lair | Medusa | anywhere | Petrifying Gaze (look away!), Serpent Volley, Stone Snakes, Venom Rain | **Gorgon's Ambush**: teleports behind the farthest hero | Head of Medusa |
| Quest for the Fleece | Colchian Dragon | anywhere | Ground Slam, Venom Breath, Tail Sweep, Burrow | **Sleepless Rampage**: three charges in a row | Golden Fleece |
| Titanomachy | Kronos | anywhere | Time Stop, Scythe Sweep, Echoes, Titan Meteors | **Time Rewind**: snaps everyone back 3s and heals | Scythe of Kronos (+ Ascension Core) |

Bosses have boss bars, scale their health with nearby players, call out every attack on screen before it lands (dodge the warning circles!), **enrage at half health** (red bar, roar, faster, attack more often, unlock their ultimate), never repeat the same attack twice in a row, never break blocks, and teleport to players who try to cheese them. Kronos also drops an Ascension Core for the top fighter.

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

## Trading (everyone)
Trade legendaries (or any god item) safely, for free or for a price:
1. Hold the relic in your main hand.
2. `/trade offer <player>` (a gift) or `/trade offer <player> <item> <count>`, e.g. `/trade offer Steve minecraft:diamond 32`.
3. They click **[ACCEPT]** or **[DECLINE]** in chat (or type `/trade accept` / `/trade decline`). Offers expire after 60s; `/trade cancel` withdraws yours.

Everything is re-checked at the moment of accepting (you still have that exact relic, they can pay and have a free relic slot), and the swap happens in one step: no scams, no dupes. Legendary trades are announced to the server.

## Allies (everyone)
Group buffs (Lyre, Battle Wisdom, War Cry, Wild Party, Feast) only reach **allies**, and your powers never hurt them. Allies are players on your `/team`, plus anyone you add:
| Command | |
|---|---|
| `/ally add <player>` | Your buffs reach them; your powers spare them |
| `/ally remove <player>` | Undo it |
| `/ally list` | See your allies |

## Controls
- **R**: Divine Power (Blessed ability). Rebind under Controls → Relics of Olympus. A HUD icon next to the hotbar shows its cooldown.
- **Jump in mid-air** with Talaria for the double jump (Survival).

## Config
`config/olympus.json` holds every number (damage, durations, cooldowns, radii, chances, item limit, loot chances, boss health, rewards). It's created with defaults on first run and synced to clients; `/olympus reload` applies changes live. Notable options: `general.maxGodItems` (3; creative players and ops ignore it unless `general.opsIgnoreLimit` is false), `general.starterTier` (netherite / diamond / iron, restart needed), `general.abilitiesAffectPlayers` (turn PvP effects off), `general.claimDelayMinutes` (anti alt-farming), `lightning.lightningFire` (off: no griefing), `events.wrathAutoStartChance`.

## Development
- `./gradlew build`: builds and runs 27 automated in-game tests
- `xvfb-run ./gradlew runClientGameTest`: plays every relic and boss in a real client
- `./gradlew runDatagen`: regenerates recipes, models, tags, loot, lang
- Bosses use tinted vanilla models; swap in Blockbench models in `BossRenderers`. See TEXTURES_TODO.md.
