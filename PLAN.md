# Relics of Olympus — Implementation Plan

Mod id `olympus` · Fabric · client + server mod

## 0. Versions (checked against meta.fabricmc.net / Mojang, 2026-11-26)

| Component | Version | Notes |
|---|---|---|
| Minecraft | **26.3** (latest stable; 26.4 is only at pre-1) | |
| Java | **25** (Mojang's manifest for 26.3 requires Java 25) | Gradle toolchain + foojay resolver auto-downloads JDK 25, so `./gradlew` works even if Debian only has JDK 21 installed |
| Fabric Loader | 0.20.1 | |
| Fabric API | 0.167.0+26.3 | |
| Fabric Loom | 1.19.x (latest stable 1.19.4) | Since 26.1 the game ships **unobfuscated**, so there's no Yarn layer and the code uses Mojang's official names. Loom uses the non-remapping plugin |
| Gradle | whatever version the official template's wrapper pins | |

The project starts from the official Fabric template layout (split `main`/`client` source sets, `fabric.mod.json`, datagen entrypoint, `./gradlew runDatagen` output in `src/main/generated`).

## 1. Package structure

```
com.olympus
├── Olympus.java                   main entrypoint (registers everything, server events)
├── config/                        OlympusConfig (Gson POJO), ConfigManager (load/save/reload, defaults)
├── registry/                      ModItems, ModComponents, ModEntities, ModRecipes, ModSounds,
│                                  ModParticles, ModEffects, ModTags, ModItemGroup, ModLoot, ModBlocks
├── god/                           God enum (12 gods + TITAN), GodItemDefinition (god, base, tier,
│                                  charm?, legendary?, primary + blessed ability), GodItems (lookup),
│                                  UpgradeLevel (MORTAL/BLESSED/ASCENDED)
├── item/                          one class per base behaviour:
│   ├── GodSwordItem, GodPickaxeItem, GodHoeItem, GodArmorItem, GodShieldItem, GodBowItem,
│   ├── GodTridentItem, CharmItem (right-click charms), GobletItem, GodSwapperItem,
│   ├── AmbrosiaItem, NectarItem, SunArrowItem, CodexItem, ...
├── ability/
│   ├── Ability (interface: id, cooldown key, activate(ctx)), AbilityContext (player, stack, level,
│   │   scale helpers for cooldown/duration/radius)
│   ├── AbilityDispatcher (keybind → main hand → offhand → armor), CooldownManager
│   ├── primary/ and blessed/      one small class per power
│   └── util/                      DivineLightning, AreaHelper, TargetFilter (players / bosses / allies),
│                                  FxHelper (particle + sound presets)
├── data/                          PlayerOlympusData (Fabric Data Attachment, persistent),
│                                  LegendaryRegistry + EventState (world SavedData)
├── limit/                         GodItemLimit (counting, sweep, pickup/slot checks), messages
├── upgrade/                       GodUpgradeRecipe (+ serializer), swapper logic
├── event/                         OlympusEvent (abstract), EventManager, one class per event,
│                                  RewardDistributor, DamageTracker
├── entity/                        bosses (TyphonEntity, CetusEntity, CerberusEntity, MedusaEntity,
│                                  ColchianDragonEntity, KronosEntity), allies (UnderworldWarrior),
│                                  projectiles (thrown god tridents, sun arrow)
├── network/                       payload records + C2S handlers (validated) + S2C sync
├── command/                       /olympus ...
├── mixin/                         server-side mixins
└── datagen/                       recipes, models, item defs, tags, loot, lang, advancements
com.olympus.client (client source set)
├── OlympusClient.java             keybind, payload receivers, HUD
├── hud/DivinePowerHud             Blessed cooldown indicator next to hotbar
├── render/                        BossRenderers (ONE place to swap placeholder → Blockbench models),
│                                  thrown god trident renderers, invisibility hiding
├── input/DoubleJumpHandler
└── mixin/client/
```

## 2. Abilities, cooldowns, keybind

- **All logic runs on the server.** The client only sends two payloads:
  - `DivinePowerC2S` (empty). The server finds the god item itself (main hand → offhand → worn armor → first match with an unlocked, non-passive Blessed ability) and checks level, cooldown and state. The client never says *which* item, so nothing can be spoofed.
  - `AirJumpC2S` (empty). The server checks that the player wears Talaria, isn't on the ground, isn't flying, gliding, riding or in water, and has air jumps left (1 Mortal, 2 Blessed). Then it applies the velocity and syncs it.
  - Rate limit: at most one packet of each type per 4 ticks per player. Extra packets are ignored.
- Right-click powers use the normal `Item#use` path, server-side.
- **Ability scaling:** `AbilityContext` exposes `cooldown(base)`, `duration(base)` and `radius(base)` and applies the level: Blessed gives cooldown ×0.75; Ascended gives cooldown ×0.5 and duration/radius ×1.25.
- **Cooldowns** are stored twice, as absolute game-time timestamps:
  1. On the **item stack** (`olympus:cooldowns` component: primary + blessed ready-time). Passing the item to a friend doesn't reset it.
  2. On the **player** (attachment, per item type). Holding two copies of the same relic doesn't let you chain casts.
  The effective cooldown is the later of the two. Both survive relogs and restarts (vanilla `ItemCooldowns` doesn't, so relogging would reset cooldowns. That's an exploit, and this closes it). The primary cooldown is mirrored into vanilla `ItemCooldowns` for the hotbar overlay, and re-applied on join.
- **Blessed cooldown HUD:** S2C `CooldownSyncS2C` sends the ready-times for the Blessed abilities of the items you hold or wear. The client draws a small icon + radial sweep next to the hotbar (the offhand side, so it doesn't collide with the vanilla offhand slot).
- **Keybind:** `KeyMapping("key.olympus.divine_power", R, category "key.categories.olympus")`, so it can be rebound in Controls.
- **FX:** every ability calls `FxHelper` presets (custom particle types + custom sound events). They're registered sounds, so resource packs can replace them.
- **Divine lightning:** every mod lightning bolt is spawned as a **visual-only** `LightningBolt`, and the mod applies its own damage. So mod lightning:
  - never hurts the caster
  - never starts fires unless `lightningFire=true`
  - never converts mobs (no farming charged creepers or witches off the Aegis)
  - respects `abilitiesAffectPlayers`
- **`abilitiesAffectPlayers=false`** turns off every *harmful* effect on other players (damage, lightning, debuffs, knockback, petrify, glowing). Buffs on allies (Lyre, Battle Wisdom, Feast, Wild Party buffs) still work.
- **Bosses and immune mobs:** tags `olympus:bosses` (Ender Dragon, Wither, Warden, Elder Guardian, all Olympus bosses) and `olympus:charm_immune`. Charm, petrify and "stop targeting" never affect these.

## 3. Player data and the 3-item limit

- `PlayerOlympusData` is a Fabric **Data Attachment**: persistent, kept across death, synced to its own client. It holds: claimed god, `starterGranted`, cooldown map, waypoint, Aegis block counter, Ares kill-streak, volley/spear charges, and similar state. Transient state (Rewind ring buffer, air jumps used, sneak timer) is kept in memory only.
- **What counts as a god item:** tag `olympus:god_items` (datagen, 24 items). Plus `olympus:upgrade_level` on every god item.
- **Counting** (`GodItemLimit.count`): main inventory + hotbar + offhand + armor + cursor + the player's 2x2 crafting grid + **god tridents the player has thrown that are still in flight** (otherwise you could throw one and grab a 4th).
- **Enforcement:**
  - Pickup: mixin on `ItemEntity#playerTouch` stops the pickup. Action bar message "The gods allow you only 3 relics", rate-limited to once per 2s.
  - Containers: mixin on `AbstractContainerMenu#doClick` / `quickMoveStack` / `Slot#mayPickup`. A god item can't move from a non-player slot (chest, result slot) into player space if that would exceed the limit. This covers crafting and smithing outputs, shift-click, number keys and double-click collect.
  - No hiding places: `Slot#mayPlace` + container `canPlaceItem` deny god items in ender chests, shulker boxes (slot + hopper path), and bundles (`BundleContents` insertion). Mobs can't pick up god items (a zombie holding a relic could despawn with it).
  - **Sweep:** every 20 ticks, plus on join, respawn, and closing a container. If you're over the limit, the extras drop at your feet with pickup delay. Kept first: armor → main hand → offhand → hotbar order → inventory order.
  - Bypass: creative mode **and** permission level ≥ 2.
  - `/give` and `/olympus give` aren't blocked; the sweep drops the extras.

## 4. Death drops and unbreakable items

- God items have **no `max_damage` component** (so no durability bar and they can never break) and the `DAMAGE_RESISTANT` (fire) component.
- Death drops use vanilla logic, so keepInventory is respected automatically.
- **God item entities:** a mixin on `ItemEntity` (tick / `hurtServer` / `fireImmune`) does this when the stack is in `olympus:god_items`:
  - never despawns (age frozen)
  - glows (`setGlowingTag`)
  - immune to fire, lava, explosions and cactus; any damage except `BYPASSES_INVULNERABILITY` (the void, `/kill`) is cancelled
- **Void destruction** of a legendary marks it as lost in `LegendaryRegistry` (detected when an item entity is removed with reason `KILLED`, or dies below the minimum build height).
- Hoplite Armor is a normal netherite-durability item.

## 5. Enchanting

- Enchantability comes through **item tags**, not code. Each god item goes into the vanilla `enchantable/*` tags its base item is in (sword, weapon, sharp_weapon, fire_aspect, mining, mining_loot, bow, trident, armor, head_armor, foot_armor, chest_armor, equippable…), and gets the `enchantable` component with netherite's value (15).
- Unbreaking and Mending target `#enchantable/durability`. God items **aren't in that tag**, so the table, the anvil and books never apply them. Nothing needs to be filtered by hand.
- Tridents that always return (Spear, Thunderbolt) are left out of the Loyalty and Riptide sets (Riptide would stop them being thrown). The Trident of Poseidon has Riptide built in, so it's left out of Riptide, Loyalty and Channeling (Channeling is always on).
- Charms (Conch, Owl, Lyre, Goblet, Mirror, Caduceus, Cornucopia, Head of Medusa) get no `enchantable` component and no tags.

## 6. Upgrades

- Component `olympus:upgrade_level` (int 0–2, default 0). The name suffix (★ / ★★), gold name, forced glint at Ascended, and tooltip lines are all built from it in `Item#getName` / tooltip.
- Custom recipe type `olympus:god_upgrade` (a `SmithingRecipe`), stored as normal JSON so datapacks can change it:
  ```json
  { "type": "olympus:god_upgrade", "template": "olympus:blessing_core",
    "base": "#olympus:god_items", "addition": "olympus:olympian_bronze_ingot",
    "from_level": 0, "to_level": 1 }
  ```
  The result copies the base stack (enchantments, name, legendary data and cooldowns stay) and bumps the level. Recipes are generated by datagen.
- Blessed abilities read the level on every activation, so changing the level mid-game takes effect right away.

## 7. Claiming and the God Swapper

- First join (`ServerPlayConnectionEvents.JOIN` + `starterGranted == false`): pick a random god, show the title "You have been claimed by Zeus!" with a custom thunder sound and a chat message, and give the starter relic (it drops at your feet if your inventory is full).
- Starter relics have no recipes.
- **God Swapper:** a use-duration item (60 ticks, channel animation). Every tick it checks that the offhand still holds a starter relic, and spins particles around the player. When the channel finishes, on the server: pick a different god, build the new relic, copy the level, copy each enchantment only if `enchantment.canEnchant(newStack)` (drop the rest), keep cooldowns, update the claimed god, show the title and sound, consume the swapper. Interrupting the channel cancels it with no cost.

## 8. Events, bosses, legendaries

- `EventManager` (SavedData): one active event at a time. It owns a `ServerBossEvent` (bar), the boss UUID, a **damage tracker** and a chunk ticket that keeps the arena loaded.
- Starting checks (`canStart(level, pos)`): Cetus needs an ocean biome **and** ≥ 10 blocks of water below, Cerberus needs the Nether. If a check fails, the admin gets a clear message.
- Start: title + chat to the whole server, boss bar for players within 96 blocks, boss spawns at the admin's position. Health = `base × (1 + healthPerExtraPlayer × (players within 64 blocks − 1))`.
- **Wrath of Zeus auto-start:** when a thunderstorm begins, roll `wrathAutoStartChance`. It needs ≥ `wrathAutoMinPlayers` online. It picks a random surviving overworld player who is above ground and outside spawn protection, gives a **60s warning title + coordinates** to everyone, then Typhon descends. (Fair: nobody gets jumped by a boss while AFK in their base.)
- **Damage tracking:** only health actually removed (capped at remaining health, so overkill doesn't count) by players. Damage dealt by a player's summoned skeletons, charmed mobs or tamed wolves counts for the owner.
- **Ending:** boss death → `RewardDistributor`. Or a timeout (`eventTimeoutMinutes`, default 30) / all players gone / `/olympus event stop` → the boss leaves dramatically, no rewards. On server restart, an active event is cancelled cleanly.
- **Rewards:** legendary → top damage dealer (dropped at their feet if they're at the limit; the item entity is protected anyway). If that legendary already exists → the top dealer gets +4 ingots and a guaranteed template instead. Everyone with ≥ 10% of the damage gets 2–4 ingots and a 25% chance at a template. Kronos also gives an Ascension Core to the top dealer.
- **LegendaryRegistry** (SavedData): legendary id → {instance UUID, first winner name, last seen holder/position/time}. "Last seen" is updated whenever the item ticks in a player inventory, as an item entity, or in a container being opened. Each legendary stack carries `olympus:legendary` {instance UUID, first owner}.
  - Duplicates (creative middle-click, dupe glitches): when a legendary stack's UUID doesn't match the registry, it's deleted, and ops are warned.
  - `/olympus legendaries` lists them; `/olympus legendaries reset <id>` frees one an admin knows is gone (`/clear`, a corrupted chunk).
- **Bosses:** each boss extends `Monster` with a small attack state machine (idle → telegraph → attack → recover). Every attack is telegraphed with particles and sound for 1–1.5s so players can dodge. None of them break blocks. They're immune to suffocation, can't ride boats or minecarts, can't be leashed, and get anti-cheese: if no player can be pathed to for 10s, they teleport to or pull the nearest one. Renderers are placeholder vanilla models scaled with the `scale` attribute, all registered in `BossRenderers`.

| Boss | Placeholder model | Attacks |
|---|---|---|
| Typhon | Blaze ×3, storm-grey | lightning ring (telegraphed circles), storm winds (pull), fireball barrage, phase 2 at 50%: calls a thunderstorm and strikes faster |
| Cetus | Elder Guardian ×3, deep blue | tidal charge, whirlpool (pull + Mining Fatigue), water-beam laser, summons drowned |
| Cerberus | Wolf ×3, black/red | triple bite combo, fire breath cone, howl (summons wither skeletons, Slowness) |
| Medusa | Witch, green | petrifying gaze (only hits if you're looking at her; looking away counters it), snake-hair poison volley, summons stone snakes (silverfish) |
| Colchian Dragon | Ravager ×2, gold-green | ground slam shockwave, poison breath, tail sweep knockback, sleepless guard (armor buff while below 30% HP) |
| Kronos | Wither Skeleton ×3, dark gold | time stop (Slowness IV pulse), scythe sweep, rewind heal (once, 20%), summons echoes (clones that die in one hit) |

## 9. Config

`config/olympus.json`. It's written with every default when missing; `/olympus reload` re-reads it (op 2). Nested sections: `general` (maxGodItems, starterTier, abilitiesAffectPlayers, lightningFire, …), `loot` (template chance), `upgrades` (multipliers), one section per item (damage/duration/cooldown/radius/chance), `events` (chance, timeout, health scaling, rewards). `starterTier` changes base attributes, so it needs a restart; everything else applies live.

## 10. Problems in the spec and my proposed fixes

**Exploits**
1. **Relogging resets vanilla cooldowns, and passing items around chains casts.** → Cooldowns are stored on both the stack and the player and persisted (§2).
2. **Thrown tridents dodge the limit**, and a thrown trident without loyalty can be picked up by anyone and left lying around. → Every thrown god trident always returns, and counts toward its owner's limit while in flight. If the owner dies or logs out, it drops as a protected item entity.
3. **Alt accounts farm starter relics** (join → claim → hand to main → repeat). On online-mode servers alts cost money. I suggest a config `claimDelayMinutes` (default 0, so it's your choice) that grants the starter relic only after that much playtime. Offline-mode servers can't be protected against this.
4. **Mace + Skybreaker / Talaria jumps:** a 15-block launch with no fall damage would allow huge mace smashes. → Divine launches and air jumps set a "divinely borne" flag that zeroes the mace's fall-distance bonus until you land normally.
5. **Volley extra arrows / Silver Bow:** extra arrows can't be picked up (no arrow dupe). Arrows with no gravity would keep flying through the air until they reach an unloaded chunk. → Gravity comes back after 5s, and drag is kept.
6. **Charm / Mirror / Bident allies on mob farms:** summoned skeletons and charmed mobs drop no loot or XP when they die, are capped at `maxAlliesPerPlayer` (8), and don't burn in daylight.
7. **Legendary duplication or silent loss** (`/clear`, creative copy, corrupted chunks): covered by the UUID check + `legendaries reset` (§8).
8. **Ambrosia:** a 12 HP instant heal, eaten repeatedly in PvP, makes fights unwinnable. → I'd add a 30s use cooldown (config, `ambrosiaCooldownSeconds`).
9. **Head of Medusa vs players:** 4s of guaranteed stun = a guaranteed kill. → On players the duration is ×0.5 (config), followed by 10s of petrify immunity. Bosses are immune.
10. **Helm of Darkness vs hacked clients:** hiding armor only in the renderer can be bypassed. → While invisible, the server stops sending your equipment to other clients (and hides the nametag), so the armor is really hidden. The Owl of Athena's glow still shows the outline: Athena sees through Hades, which is nice counterplay.

**Won't work with vanilla mechanics as written**
11. **Nausea and Blindness do nothing to mobs** (Goblet Wild Party, Thyrsus, Lyre Sunburst). → A custom `olympus:intoxicated` effect: on players it shows Nausea; on mobs it makes them stagger randomly and drop their target every ~1s. Blindness on mobs is done as "loses its target and can't re-acquire it for 3s". Players still get real Blindness/Nausea (if PvP effects are on).
12. **The Head of Medusa as a mob head would be placeable**, turning a unique legendary into a block. → It's wearable in the head slot but can't be placed.
13. **Trident of Poseidon:** in vanilla, Riptide replaces throwing entirely. → Hold and release = Riptide launch anywhere; **sneak** + release = a throw with Channeling. "Channeling always on" in any weather would put lightning on every hit, so there's an internal 10s lightning cooldown (config).
14. **Lightning side effects** (charged creepers, witches, fire): handled by divine lightning (§2).
15. **"Sneak 2s" for Helm of Darkness** would go off when you crouch-bridge and waste a 2-minute cooldown. → It triggers only if you're sneaking **and standing still** for 2s (config `helmRequireStill`, default true).
16. **Sun Arrows "light up where they land":** placing vanilla light blocks risks leaving permanent ones. → A custom invisible `olympus:sun_glow` block that's only placed in air and removes itself with a scheduled block tick (those persist with the chunk, so none are left behind).
17. **Rewind** works within the same dimension only. If you changed dimension, the cast fails without starting the cooldown.
18. **Golden Fleece death protection** follows totem rules: it doesn't save you from `/kill` or the void.

**Balance notes (no change unless you want one)**
- One **Olympian Bronze Block** costs 9 netherite scrap + 36 gold blocks + 36 copper blocks, and every craftable relic needs one. The Ascension Core needs 2. It's very expensive, but gold farms make gold cheap, so scrap is the real bottleneck. That seems right to me.
- **Hoplite** full set = Resistance I + 0.9 knockback resistance. It can't be combined with Helm/Talaria/Fleece (they take armor slots), which is a good natural trade-off.
- **Goblet / Wild Party effect pool** excludes Hero of the Village (trade exploit), Saturation (free food), Invisibility and Conduit Power. The pool is a config list.
- **starterTier netherite** gives brand-new players a netherite sword/pickaxe/helmet/boots on day one, depending on the god. That's a big gap between gods (Ares gets a sword, Apollo gets a lyre). It's fine for an SMP that's already running. For a fresh world I'd pick `diamond`.
- Combat logging (log out to keep relics safe) is out of scope, but could be a later option.

## 11. Phases (each ends with `./gradlew build`, commit, test steps, wait)

1. Template setup, config, materials, template loot, claiming, 12 starter relics + primary powers + netherite stats, placeholder textures, `TEXTURES_TODO.md`.
2. Limit, death and item-entity rules, unbreakable, enchanting tags, upgrade levels + smithing, Divine Power key + HUD, cooldown sync, God Swapper, 12 Blessed abilities, admin commands (claim/give/setlevel/reload).
3. Craftable items (Caduceus, Spear, Forge Hammer, Harpe, Thyrsus, Cornucopia, Ambrosia, Nectar, Sun Arrows, Hoplite).
4. Event framework, 6 bosses, 6 legendaries, uniqueness registry, event/legendary commands.
5. Polish: balance pass, unique FX, creative tab review, full lang, Codex of Olympus, README.md.

Recipes: see **RECIPES.md** (please approve along with this plan).
