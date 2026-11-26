#!/usr/bin/env python3
"""Renders every Relics of Olympus recipe as a crafting-grid picture (docs/recipes/*.png).

    python3 tools/render_recipes.py <path-to-extracted-vanilla-assets>

The vanilla textures come from your own Minecraft client jar (assets/minecraft/textures/...).
"""
import glob, json, os, sys
from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
VANILLA = os.path.join(sys.argv[1], "assets", "minecraft", "textures")
RECIPES = os.path.join(ROOT, "src/main/generated/data/olympus/recipe")
LANG = json.load(open(os.path.join(ROOT, "src/main/generated/assets/olympus/lang/en_us.json")))
OUT = os.path.join(ROOT, "docs", "recipes")
BOLD = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 17)
REG = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 13)
SMALL = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 12)

# Olympus items look like these vanilla items (see OlympusModelProvider).
LOOKS = {
    "olympian_bronze_ingot": "item/copper_ingot", "divine_smithing_template": "item/netherite_upgrade_smithing_template",
    "blessing_core": "item/heart_of_the_sea", "ascension_core": "item/nether_star", "god_swapper": "item/ender_eye",
    "laurel_of_olympus": "item/totem_of_undying", "gorgoneion_banner_pattern": "item/skull_banner_pattern",
    "thunderbolt_banner_pattern": "item/flow_banner_pattern", "caduceus": "item/breeze_rod", "forge_hammer": "item/mace",
    "harpe": "item/golden_sword", "thyrsus": "item/blaze_rod", "cornucopia": "item/goat_horn", "ambrosia": "item/golden_apple",
    "nectar": "item/honey_bottle", "sun_arrow": "item/spectral_arrow", "hoplite_helmet": "item/netherite_helmet",
    "hoplite_chestplate": "item/netherite_chestplate", "hoplite_leggings": "item/netherite_leggings", "hoplite_boots": "item/netherite_boots",
    "codex_of_olympus": "item/written_book", "spear_of_athena": "item/trident", "owl_of_athena": "item/feather",
    "hammer_of_hephaestus": "item/netherite_pickaxe", "helm_of_darkness": "item/netherite_helmet", "sickle_of_demeter": "item/netherite_hoe",
    "talaria": "item/leather_boots", "goblet_of_dionysus": "item/potion",
}
GLINT = {"blessing_core", "ascension_core", "god_swapper", "laurel_of_olympus", "ambrosia", "enchanted_golden_apple"}
VANILLA_TEX = {"enchanted_golden_apple": "item/golden_apple", "hay_block": "block/hay_block_side", "goat_horn": "item/goat_horn",
               "lightning_rod": "block/lightning_rod", "heavy_core": "block/heavy_core", "vine": "block/vine"}
TINT = {"talaria": (216, 180, 90), "vine": (72, 181, 24), "goblet_of_dionysus": (122, 20, 80)}

BG, SLOT, SLOT_DARK, SLOT_LIGHT, FG, MUTED, GOLD = (198, 198, 198), (139, 139, 139), (55, 55, 55), (255, 255, 255), (40, 40, 40), (90, 90, 90), (176, 120, 20)
S = 40  # slot size


def name_of(id_):
    ns, path = id_.split(":")
    if ns == "olympus":
        if path == "divine_smithing_template":
            return "Divine Smithing Template"
        return LANG.get(f"item.olympus.{path}") or LANG.get(f"block.olympus.{path}") or path
    return path.replace("_", " ").title()


def texture(id_):
    ns, path = id_.split(":")
    if ns == "olympus":
        if path == "olympian_bronze_block":
            p = os.path.join(ROOT, "src/main/resources/assets/olympus/textures/block/olympian_bronze_block.png")
        else:
            p = os.path.join(VANILLA, LOOKS.get(path, "item/barrier") + ".png")
    else:
        rel = VANILLA_TEX.get(path)
        p = os.path.join(VANILLA, rel + ".png") if rel else None
        if not p or not os.path.exists(p):
            p = os.path.join(VANILLA, "item", path + ".png")
        if not os.path.exists(p):
            p = os.path.join(VANILLA, "block", path + ".png")
    img = Image.open(p).convert("RGBA")
    img = img.crop((0, 0, 16, 16)) if img.size != (16, 16) else img  # animated textures: first frame
    tint = TINT.get(path)
    if tint:
        r, g, b, a = img.split()
        gray = Image.merge("RGB", (r, g, b)).convert("L")
        colored = Image.merge("RGB", [gray.point(lambda v, c=c: v * c // 255) for c in tint])
        if path == "goblet_of_dionysus":  # potion: liquid overlay tinted, bottle on top
            bottle = Image.open(os.path.join(VANILLA, "item/potion.png")).convert("RGBA")
            ov = Image.open(os.path.join(VANILLA, "item/potion_overlay.png")).convert("RGBA")
            r2, g2, b2, a2 = ov.split()
            g_ = Image.merge("RGB", (r2, g2, b2)).convert("L")
            liquid = Image.merge("RGBA", [g_.point(lambda v, c=c: v * c // 255) for c in tint] + [a2])
            img = Image.alpha_composite(liquid, bottle)
        else:
            img = Image.merge("RGBA", (*colored.split(), a))
            if path == "talaria":
                img = Image.alpha_composite(img, Image.open(os.path.join(VANILLA, "item/leather_boots_overlay.png")).convert("RGBA"))
    if path in GLINT:
        glow = Image.new("RGBA", img.size, (190, 120, 255, 70))
        img = Image.composite(Image.alpha_composite(img, glow), img, img.split()[3])
    return img.resize((32, 32), Image.NEAREST)


def ingredient_ids(value):
    if isinstance(value, list):
        return [v for v in value]
    return [value]


def draw_slot(d, img, x, y, item=None, count=1):
    d.rectangle([x, y, x + S - 1, y + S - 1], fill=SLOT)
    d.line([x, y, x + S - 1, y], fill=SLOT_DARK); d.line([x, y, x, y + S - 1], fill=SLOT_DARK)
    d.line([x, y + S - 1, x + S - 1, y + S - 1], fill=SLOT_LIGHT); d.line([x + S - 1, y, x + S - 1, y + S - 1], fill=SLOT_LIGHT)
    if item:
        if item.startswith("#"):
            tex = texture("olympus:spear_of_athena")  # example relic
            img.paste(tex, (x + 4, y + 4), tex)
            d.text((x + S // 2, y + S - 2), "any", font=SMALL, fill=GOLD, anchor="ms", stroke_width=2, stroke_fill=(40, 40, 40))
        else:
            tex = texture(item)
            img.paste(tex, (x + 4, y + 4), tex)
        if count > 1:
            d.text((x + S - 4, y + S - 3), str(count), font=BOLD, fill=(255, 255, 255), anchor="rs", stroke_width=2, stroke_fill=(60, 60, 60))


def card(recipe_id, r):
    t = r["type"].split(":")[1]
    W, H = 560, 250
    img = Image.new("RGBA", (W, H), BG)
    d = ImageDraw.Draw(img)
    result = r.get("result", {}).get("id") if isinstance(r.get("result"), dict) else None
    legend = {}
    if t == "god_upgrade":
        title = "Blessed ★ upgrade" if r["to_level"] == 1 else "Ascended ★★ upgrade"
        slots = [r["template"], r["base"], r["addition"]]
        result_label = "Same relic, " + ("Blessed ★" if r["to_level"] == 1 else "Ascended ★★")
    else:
        title = name_of(result)
        if r.get("result", {}).get("count", 1) > 1:
            title += f"  ×{r['result']['count']}"
    d.text((14, 10), title, font=BOLD, fill=FG)
    d.text((14, 32), "Smithing table" if t in ("smithing_transform", "god_upgrade") else "Crafting table", font=REG, fill=MUTED)
    gx, gy = 14, 56
    if t == "crafting_shaped":
        for row, line in enumerate(r["pattern"]):
            for col, ch in enumerate(line):
                item = None
                if ch != " ":
                    item = ingredient_ids(r["key"][ch])[0]
                    legend[item] = True
                draw_slot(d, img, gx + col * S, gy + row * S, item)
        for row in range(len(r["pattern"]), 3):
            for col in range(3):
                draw_slot(d, img, gx + col * S, gy + row * S)
        for row in range(3):
            for col in range(len(r["pattern"][0]) if row < len(r["pattern"]) else 0, 3):
                if row < len(r["pattern"]):
                    draw_slot(d, img, gx + col * S, gy + row * S)
        ax = gx + 3 * S + 14
    elif t == "crafting_shapeless":
        items = [ingredient_ids(i)[0] for i in r["ingredients"]]
        for k in range(9):
            item = items[k] if k < len(items) else None
            if item: legend[item] = True
            draw_slot(d, img, gx + (k % 3) * S, gy + (k // 3) * S, item)
        ax = gx + 3 * S + 14
    else:  # smithing
        slots = [r["template"], r["base"], r["addition"]]
        for k, item in enumerate(slots):
            it = ingredient_ids(item)[0]
            legend[it] = True
            draw_slot(d, img, gx + k * (S + 6), gy + S, it)
        ax = gx + 3 * (S + 6) + 8
    # arrow + result
    ay = gy + S + S // 2
    d.polygon([(ax, ay - 6), (ax + 22, ay - 6), (ax + 22, ay - 13), (ax + 36, ay), (ax + 22, ay + 13), (ax + 22, ay + 6), (ax, ay + 6)], fill=(110, 110, 110))
    rx = ax + 48
    if t == "god_upgrade":
        draw_slot(d, img, rx, gy + S, r["base"])
        d.text((rx - 4, gy + 2 * S + 6), result_label, font=SMALL, fill=GOLD)
    else:
        draw_slot(d, img, rx, gy + S, result, r.get("result", {}).get("count", 1))
    # legend
    lx = rx + S + 22
    ly = 56
    for item in legend:
        label = ("Any god item (must be Blessed ★)" if t == "god_upgrade" and r["to_level"] == 2 else "Any god item (relic)") if item.startswith("#") else name_of(item)
        if True:
            tex = texture("olympus:spear_of_athena" if item.startswith("#") else item).resize((16, 16), Image.NEAREST)
            img.paste(tex, (lx, ly), tex)
        d.text((lx + 22, ly + 1), label, font=REG, fill=FG)
        ly += 21
    return img


GROUPS = [
    ("Materials", ["olympian_bronze_ingot_from_alloy", "olympian_bronze_block", "olympian_bronze_ingot", "divine_smithing_template",
                   "blessing_core", "ascension_core", "god_swapper", "laurel_of_olympus"]),
    ("Upgrades", ["blessed_upgrade", "ascended_upgrade"]),
    ("Craftable god items", ["caduceus", "spear_of_athena", "forge_hammer", "harpe", "thyrsus", "cornucopia"]),
    ("Consumables, ammo and armor", ["ambrosia", "nectar", "sun_arrow", "hoplite_helmet_smithing", "hoplite_chestplate_smithing",
                                     "hoplite_leggings_smithing", "hoplite_boots_smithing"]),
    ("Other", ["codex_of_olympus", "gorgoneion_banner_pattern", "thunderbolt_banner_pattern"]),
]

os.makedirs(OUT, exist_ok=True)
for i, (title, ids) in enumerate(GROUPS, 1):
    cards = [card(rid, json.load(open(os.path.join(RECIPES, rid + ".json")))) for rid in ids]
    cols = 2
    rows = (len(cards) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * 570 + 10, 60 + rows * 260), (40, 34, 28))
    d = ImageDraw.Draw(sheet)
    d.text((16, 16), f"Relics of Olympus: {title}", font=ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 24), fill=(240, 200, 110))
    for k, c in enumerate(cards):
        sheet.paste(c, (10 + (k % cols) * 570, 56 + (k // cols) * 260))
    path = os.path.join(OUT, f"{i}_{title.split(',')[0].lower().replace(' ', '_')}.png")
    sheet.convert("RGB").save(path)
    print(path)
