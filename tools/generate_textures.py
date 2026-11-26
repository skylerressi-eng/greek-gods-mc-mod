#!/usr/bin/env python3
"""
Generates the placeholder textures for Relics of Olympus.

Every texture is simple pixel art (a shape, an outline and a little shading) so the mod is
playable before real art exists. Replace any PNG with your own art; this script only writes
files that do not exist yet unless you pass --force.

    python3 tools/generate_textures.py           # create missing textures
    python3 tools/generate_textures.py --force   # regenerate everything

Requires Pillow (pip install pillow).
"""
import math
import os
import sys

from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets", "olympus", "textures")
FORCE = "--force" in sys.argv


def hexc(h, a=255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (c[3],)


def save(img, rel):
    path = os.path.join(ROOT, rel)
    if os.path.exists(path) and not FORCE:
        return
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    print("wrote", os.path.relpath(path, os.path.join(ROOT, "..", "..", "..", "..", "..")))


def canvas(size=16):
    return Image.new("RGBA", (size, size), (0, 0, 0, 0))


def finish(img, outline_color, light=True):
    """Adds a 1px dark outline around the shape and a soft top-left highlight."""
    w, h = img.size
    px = img.load()
    filled = [[px[x, y][3] > 0 for y in range(h)] for x in range(w)]
    out = img.copy()
    opx = out.load()
    for x in range(w):
        for y in range(h):
            if filled[x][y]:
                if light:
                    # highlight pixels whose top-left neighbour is empty, darken bottom-right edges
                    tl = x > 0 and y > 0 and not filled[x - 1][y - 1]
                    br = x < w - 1 and y < h - 1 and not filled[x + 1][y + 1]
                    if tl:
                        opx[x, y] = shade(px[x, y], 1.25)
                    elif br:
                        opx[x, y] = shade(px[x, y], 0.75)
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < w and 0 <= ny < h and filled[nx][ny]:
                    opx[x, y] = outline_color
                    break
    return out


# ─── Item textures ────────────────────────────────────────────────────────────

def ingot(c1):
    img = canvas(); d = ImageDraw.Draw(img)
    d.polygon([(2, 10), (5, 6), (13, 6), (14, 10), (12, 12), (3, 12)], fill=c1)
    d.line([(5, 7), (12, 7)], fill=shade(c1, 1.35))
    return finish(img, shade(c1, 0.35))


def template(c1, accent):
    img = canvas(); d = ImageDraw.Draw(img)
    d.rectangle([3, 1, 12, 14], fill=c1)
    d.rectangle([4, 2, 11, 13], fill=shade(c1, 0.85))
    # lightning glyph
    d.line([(9, 3), (6, 8), (9, 8), (6, 12)], fill=accent, width=1)
    return finish(img, shade(c1, 0.3))


def orb(c1, stars=1):
    img = canvas(); d = ImageDraw.Draw(img)
    d.ellipse([2, 2, 13, 13], fill=c1)
    d.ellipse([4, 4, 9, 9], fill=shade(c1, 1.4))
    white = hexc("ffffff")
    for i in range(stars):
        cx = 8 if stars == 1 else 6 + i * 4
        d.point([(cx, 6), (cx - 1, 7), (cx, 7), (cx + 1, 7), (cx, 8)], fill=white)
    return finish(img, shade(c1, 0.3))


def swapper():
    img = canvas(); d = ImageDraw.Draw(img)
    a, b = hexc("e8c25a"), hexc("5ab4e8")
    d.pieslice([2, 2, 13, 13], 90, 270, fill=a)
    d.pieslice([2, 2, 13, 13], 270, 90, fill=b)
    d.ellipse([5, 2, 10, 7], fill=b)
    d.ellipse([5, 8, 10, 13], fill=a)
    d.point([(7, 4), (8, 4), (7, 10), (8, 10)], fill=hexc("ffffff"))
    return finish(img, hexc("2a2030"))


def banner_pattern_item(accent):
    img = canvas(); d = ImageDraw.Draw(img)
    paper = hexc("e8dcc0")
    d.rectangle([3, 2, 12, 14], fill=paper)
    d.rectangle([5, 4, 10, 11], fill=accent)
    return finish(img, hexc("5a4a30"))


def conch():
    img = canvas(); d = ImageDraw.Draw(img)
    c = hexc("f2c6a8")
    d.polygon([(2, 12), (6, 4), (11, 2), (14, 6), (12, 11), (6, 14)], fill=c)
    for i, (x, y) in enumerate([(10, 4), (11, 6), (10, 8), (8, 9), (6, 10)]):
        d.point((x, y), fill=hexc("c4846a"))
    d.polygon([(2, 12), (5, 10), (5, 13)], fill=hexc("2eb8b0"))
    return finish(img, hexc("5a3424"))


def helmet(c1, accent):
    img = canvas(); d = ImageDraw.Draw(img)
    d.rectangle([3, 4, 12, 9], fill=c1)
    d.rectangle([3, 10, 5, 13], fill=c1)
    d.rectangle([10, 10, 12, 13], fill=c1)
    d.rectangle([6, 9, 9, 10], fill=c1)
    d.line([(4, 3), (11, 3)], fill=c1)
    d.line([(7, 1), (8, 1), (7, 2), (8, 2)], fill=accent)  # crest
    d.rectangle([7, 1, 8, 3], fill=accent)
    d.point([(5, 8), (10, 8)], fill=hexc("b07cff"))  # glowing eye slits
    return finish(img, hexc("0c0610"))


def owl():
    img = canvas(); d = ImageDraw.Draw(img)
    c = hexc("b8bcc4")
    d.ellipse([3, 4, 12, 14], fill=c)
    d.polygon([(3, 5), (5, 2), (6, 5)], fill=c)
    d.polygon([(9, 5), (10, 2), (12, 5)], fill=c)
    d.ellipse([4, 6, 7, 9], fill=hexc("ffd84a"))
    d.ellipse([8, 6, 11, 9], fill=hexc("ffd84a"))
    d.point([(5, 7), (9, 7)], fill=hexc("101010"))
    d.point([(7, 9), (8, 9), (7, 10)], fill=hexc("e0a040"))
    return finish(img, hexc("3a3c44"))


def sword(blade, hilt, guard):
    img = canvas(); d = ImageDraw.Draw(img)
    for i in range(9):
        d.point((5 + i, 10 - i), fill=blade)
        d.point((6 + i, 10 - i), fill=shade(blade, 0.8))
    d.point((14, 1), fill=blade)
    d.line([(2, 9), (6, 13)], fill=guard)
    d.line([(4, 11), (2, 13)], fill=hilt)
    d.point((1, 14), fill=guard)
    return finish(img, shade(blade, 0.25), light=False)


def lyre():
    img = canvas(); d = ImageDraw.Draw(img)
    g = hexc("ffc93c")
    d.line([(3, 2), (3, 10), (5, 13), (10, 13), (12, 10), (12, 2)], fill=g, width=2)
    d.line([(3, 3), (12, 3)], fill=g)
    for x in (6, 8, 10):
        d.line([(x - 1 if x == 6 else x, 4), (x - 1 if x == 6 else x, 12)], fill=hexc("fff4d0"))
    return finish(img, hexc("6a4a10"), light=False)


def bow(c1, pull=0):
    img = canvas(); d = ImageDraw.Draw(img)
    bend = [0, 1, 2, 3][pull]
    d.arc([1 + bend, 1 + bend, 22 - bend, 22 - bend], 180, 270, fill=c1, width=2)
    sx = 3 + bend * 2
    d.line([(sx, 13 - bend), (13 - bend, sx)], fill=hexc("e8f0ff"))
    if pull > 0:
        d.line([(12 - pull, 12 - pull), (4, 4)], fill=hexc("8a6a4a"))
        d.point((4, 4), fill=hexc("c8d8e8"))
    return finish(img, hexc("2a3440"), light=False)


def boot(c1, wing):
    img = canvas(); d = ImageDraw.Draw(img)
    d.rectangle([5, 4, 9, 11], fill=c1)
    d.rectangle([5, 11, 13, 13], fill=c1)
    d.polygon([(4, 5), (1, 3), (1, 6), (2, 8), (4, 8)], fill=wing)
    d.polygon([(10, 5), (13, 2), (14, 5), (12, 8), (10, 8)], fill=wing)
    return finish(img, hexc("3a2a10"))


def hammer(head, handle):
    img = canvas(); d = ImageDraw.Draw(img)
    d.line([(3, 13), (10, 6)], fill=handle, width=2)
    d.polygon([(7, 3), (10, 1), (14, 5), (12, 8)], fill=head)
    d.point([(11, 4), (12, 5)], fill=hexc("ffe0a0"))
    return finish(img, hexc("301808"), light=False)


def sickle(blade, handle):
    img = canvas(); d = ImageDraw.Draw(img)
    d.arc([3, 1, 14, 12], 200, 360, fill=blade, width=2)
    d.line([(3, 14), (7, 9)], fill=handle, width=2)
    d.point([(4, 6)], fill=blade)
    return finish(img, hexc("1a2a10"), light=False)


def goblet():
    img = canvas(); d = ImageDraw.Draw(img)
    g = hexc("e8b84a")
    d.polygon([(3, 2), (12, 2), (11, 6), (8, 8), (7, 8), (4, 6)], fill=g)
    d.polygon([(4, 3), (11, 3), (10, 5), (5, 5)], fill=hexc("8a1e6e"))
    d.rectangle([7, 8, 8, 11], fill=g)
    d.rectangle([4, 12, 11, 13], fill=g)
    return finish(img, hexc("4a3010"))


def mirror():
    img = canvas(); d = ImageDraw.Draw(img)
    frame = hexc("ff7ab6")
    d.ellipse([3, 1, 12, 10], fill=frame)
    d.ellipse([5, 3, 10, 8], fill=hexc("d8f0ff"))
    d.point([(6, 4), (7, 4), (6, 5)], fill=hexc("ffffff"))
    d.rectangle([7, 10, 8, 14], fill=hexc("e8b84a"))
    return finish(img, hexc("5a1838"))


def bronze_block():
    img = Image.new("RGBA", (16, 16), hexc("b0783a"))
    d = ImageDraw.Draw(img)
    hi, lo, mid = hexc("e0a868"), hexc("6a4018"), hexc("c88a48")
    d.rectangle([0, 0, 15, 15], outline=lo)
    d.line([(1, 1), (14, 1)], fill=hi)
    d.line([(1, 1), (1, 14)], fill=hi)
    # Greek key (meander) band
    for x0 in (2, 8):
        d.line([(x0, 6), (x0 + 4, 6), (x0 + 4, 10), (x0 + 1, 10), (x0 + 1, 8), (x0 + 2, 8)], fill=lo)
    d.line([(2, 3), (13, 3)], fill=mid)
    d.line([(2, 12), (13, 12)], fill=mid)
    return img


# ─── Particles (8x8) ──────────────────────────────────────────────────────────

def particle(kind, color):
    img = canvas(8); d = ImageDraw.Draw(img)
    c = hexc(color)
    soft = (c[0], c[1], c[2], 140)
    if kind == "star":
        d.line([(3, 0), (3, 7)], fill=soft); d.line([(0, 3), (7, 3)], fill=soft)
        d.rectangle([2, 2, 4, 4], fill=c); d.point((3, 3), fill=hexc("ffffff"))
    elif kind == "dot":
        d.ellipse([1, 1, 6, 6], fill=soft); d.ellipse([2, 2, 5, 5], fill=c)
    elif kind == "drop":
        d.polygon([(3, 0), (5, 4), (3, 7), (1, 4)], fill=c); d.point((3, 3), fill=hexc("ffffff", 200))
    elif kind == "blob":
        d.ellipse([0, 0, 7, 7], fill=(c[0], c[1], c[2], 90)); d.ellipse([2, 2, 5, 5], fill=(c[0], c[1], c[2], 170))
    elif kind == "feather":
        d.line([(1, 7), (6, 0)], fill=shade(c, 0.7))
        d.polygon([(2, 5), (5, 0), (7, 1), (3, 6)], fill=c)
    elif kind == "leaf":
        d.ellipse([1, 2, 6, 5], fill=c); d.line([(1, 6), (5, 2)], fill=shade(c, 0.6))
    elif kind == "petal":
        d.ellipse([1, 1, 6, 5], fill=c); d.point((3, 2), fill=hexc("ffffff", 180))
    elif kind == "crescent":
        d.ellipse([0, 0, 7, 7], fill=c); d.ellipse([2, -1, 8, 6], fill=(0, 0, 0, 0))
    elif kind == "swirl":
        d.arc([0, 0, 7, 7], 0, 270, fill=c); d.arc([2, 2, 5, 5], 90, 360, fill=soft)
    elif kind == "zap":
        d.line([(5, 0), (2, 3), (5, 4), (2, 7)], fill=c); d.point((3, 3), fill=hexc("ffffff"))
    return img


# ─── Equipment (worn armor, 64x32 humanoid layer) ─────────────────────────────

def equipment(base, accent):
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Head (helmet) region: 0..32 x 0..16, legs/feet region: 0..16 x 16..32
    d.rectangle([0, 0, 31, 15], fill=base)
    d.rectangle([32, 0, 63, 15], fill=(0, 0, 0, 0))
    d.rectangle([0, 16, 15, 31], fill=base)
    for x in range(0, 32, 4):
        d.line([(x, 0), (x, 15)], fill=shade(base, 0.8))
    d.line([(8, 7), (15, 7)], fill=accent)  # brow band
    d.line([(0, 26), (15, 26)], fill=accent)  # sandal straps
    d.line([(0, 29), (15, 29)], fill=accent)
    return img


# ─── Banner / shield patterns (white masks, tinted by dye) ────────────────────

def pattern(design, shield):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    faces = [(2, 2, 10, 20)] if shield else [(1, 1, 20, 40), (22, 1, 20, 40)]
    for (fx, fy, fw, fh) in faces:
        layer = Image.new("RGBA", (fw * 4, fh * 4), (0, 0, 0, 0))
        d = ImageDraw.Draw(layer)
        W, H = layer.size
        white = (255, 255, 255, 255)
        if design == "gorgoneion":
            cx, cy, r = W / 2, H * 0.45, W * 0.24
            # snake hair: wavy rays
            for i in range(10):
                a = math.pi * 2 * i / 10
                pts = []
                for t in range(0, 9):
                    rr = r * 1.05 + t * W * 0.03
                    wob = math.sin(t * 1.3 + i) * 0.25
                    pts.append((cx + math.cos(a + wob) * rr, cy + math.sin(a + wob) * rr))
                d.line(pts, fill=white, width=max(2, W // 16))
            d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=white)
            e = r * 0.28
            for ex in (cx - r * 0.4, cx + r * 0.4):
                d.ellipse([ex - e, cy - e * 0.8, ex + e, cy + e * 0.8], fill=(0, 0, 0, 0))
            d.rectangle([cx - r * 0.35, cy + r * 0.45, cx + r * 0.35, cy + r * 0.55], fill=(0, 0, 0, 0))
        elif design == "thunderbolt":
            pts = [(W * 0.62, H * 0.05), (W * 0.30, H * 0.52), (W * 0.50, H * 0.52), (W * 0.36, H * 0.95),
                   (W * 0.72, H * 0.42), (W * 0.52, H * 0.42), (W * 0.70, H * 0.05)]
            d.polygon(pts, fill=white)
        small = layer.resize((fw, fh), Image.NEAREST)
        img.paste(small, (fx, fy), small)
    return img


def main():
    items = {
        "olympian_bronze_ingot": ingot(hexc("c8843c")),
        "divine_smithing_template": template(hexc("d8d0b8"), hexc("ffd84a")),
        "blessing_core": orb(hexc("4ad8e8"), 1),
        "ascension_core": orb(hexc("ffb43a"), 2),
        "god_swapper": swapper(),
        "gorgoneion_banner_pattern": banner_pattern_item(hexc("8a6a3a")),
        "thunderbolt_banner_pattern": banner_pattern_item(hexc("4a8ad8")),
        "conch_of_the_deep": conch(),
        "helm_of_darkness": helmet(hexc("2a1c34"), hexc("8a5cc9")),
        "owl_of_athena": owl(),
        "blade_of_ares": sword(hexc("d8d8e0"), hexc("5a1a10"), hexc("d13b2e")),
        "lyre_of_apollo": lyre(),
        "silver_bow": bow(hexc("c8d8e8"), 0),
        "silver_bow_pulling_0": bow(hexc("c8d8e8"), 1),
        "silver_bow_pulling_1": bow(hexc("c8d8e8"), 2),
        "silver_bow_pulling_2": bow(hexc("c8d8e8"), 3),
        "talaria": boot(hexc("a8743a"), hexc("f8f8f0")),
        "hammer_of_hephaestus": hammer(hexc("ff7a1a"), hexc("5a3a20")),
        "sickle_of_demeter": sickle(hexc("d8e0c8"), hexc("6cbf3a")),
        "goblet_of_dionysus": goblet(),
        "mirror_of_aphrodite": mirror(),
    }
    for name, img in items.items():
        save(img, f"item/{name}.png")

    save(bronze_block(), "block/olympian_bronze_block.png")

    particles = {
        "divine_glint": ("star", "ffe680"),
        "storm_spark": ("zap", "aee8ff"),
        "tide_drop": ("drop", "3ec8d0"),
        "shade": ("blob", "4a2a6a"),
        "owl_feather": ("feather", "e0e4ec"),
        "war_ember": ("dot", "ff4a2a"),
        "sun_mote": ("dot", "ffd84a"),
        "moonbeam": ("crescent", "d8ecff"),
        "wind_wisp": ("swirl", "f8f8f0"),
        "forge_spark": ("star", "ff9a2a"),
        "grain_leaf": ("leaf", "7ccf4a"),
        "wine_drop": ("drop", "a03cbf"),
        "rose_petal": ("petal", "ff8ac0"),
        "titan_dust": ("blob", "b08a3c"),
    }
    for name, (kind, color) in particles.items():
        save(particle(kind, color), f"particle/{name}.png")

    save(equipment(hexc("241830"), hexc("8a5cc9")), "entity/equipment/humanoid/helm_of_darkness.png")
    save(equipment(hexc("a8743a"), hexc("f8f8f0")), "entity/equipment/humanoid/talaria.png")

    for design in ("gorgoneion", "thunderbolt"):
        save(pattern(design, False), f"entity/banner/{design}.png")
        save(pattern(design, True), f"entity/shield/{design}.png")


if __name__ == "__main__":
    main()
