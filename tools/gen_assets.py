#!/usr/bin/env python3
"""Genera texturas, modelos, recetas y lang de HeroForge (heroes ORIGINALES)."""
import json, os, random
from PIL import Image, ImageDraw

ROOT = "/home/claude/heroforge-mod/src/main/resources"
NS = "heroforge"
A = f"{ROOT}/assets/{NS}"
D = f"{ROOT}/data/{NS}"

# id, nombre ES, nombre EN, primario, secundario, acento, ingrediente especial, emblema
HEROES = [
    ("relampago", "Relámpago", "Flash Runner", (200, 30, 40), (240, 190, 40), (255, 240, 150), "minecraft:gold_ingot", "bolt"),
    ("centinela", "Centinela Alado", "Winged Sentinel", (40, 90, 200), (230, 230, 240), (140, 200, 255), "minecraft:feather", "wing"),
    ("tejedor", "Tejedor", "Weaver", (160, 30, 120), (30, 30, 50), (240, 240, 240), "minecraft:string", "web"),
    ("bastion", "Bastión", "Bastion", (90, 100, 110), (200, 140, 30), (220, 225, 230), "minecraft:iron_block", "shield"),
    ("espectro", "Espectro", "Specter", (35, 30, 55), (110, 60, 190), (180, 140, 255), "minecraft:ender_pearl", "eye"),
    ("ignea", "Ígnea", "Ember", (210, 70, 20), (255, 180, 30), (255, 240, 120), "minecraft:blaze_powder", "flame"),
    ("glaciar", "Glaciar", "Glacier", (120, 200, 235), (240, 250, 255), (255, 255, 255), "minecraft:packed_ice", "snow"),
    ("voltio", "Voltio", "Volt", (30, 140, 90), (250, 235, 60), (190, 255, 210), "minecraft:redstone_block", "spark"),
]
PIECES = ["helmet", "chestplate", "leggings", "boots"]
PIECE_ES = {"helmet": "Casco", "chestplate": "Peto", "leggings": "Pantalones", "boots": "Botas"}
PIECE_EN = {"helmet": "Helmet", "chestplate": "Chestplate", "leggings": "Leggings", "boots": "Boots"}
PIECE_COST = {"helmet": 5, "chestplate": 8, "leggings": 7, "boots": 4}


def w(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        if isinstance(data, (dict, list)):
            json.dump(data, f, indent=2, ensure_ascii=False)
        else:
            f.write(data)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)


# ---------- emblemas 7x7 (matrices) ----------
EMB = {
    "bolt": ["...##..", "..##...", ".###...", "..####.", "...##..", "..##...", "..#...."],
    "wing": ["#.....#", "##...##", "###.###", ".#####.", "..###..", "...#...", "......."],
    "web": ["#.#.#.#", ".#####.", "#######", ".#####.", "#######", ".#####.", "#.#.#.#"],
    "shield": ["#######", "#######", "#######", ".#####.", "..###..", "...#...", "......."],
    "eye": ["..###..", ".#...#.", "#.###.#", "#.###.#", ".#...#.", "..###..", "......."],
    "flame": ["...#...", "..##...", "..###..", ".####.#", ".#####.", ".#####.", "..###.."],
    "snow": ["...#...", "#..#..#", ".#.#.#.", "#######", ".#.#.#.", "#..#..#", "...#..."],
    "spark": ["..###..", ".##.##.", "##...##", "#..#..#", "##...##", ".##.##.", "..###.."],
}


def draw_emblem(img, name, ox, oy, col):
    for y, row in enumerate(EMB[name]):
        for x, ch in enumerate(row):
            if ch == "#":
                img.putpixel((ox + x, oy + y), col)


def base_fill(img, box, pri, sec, seed):
    rnd = random.Random(seed)
    x0, y0, x1, y1 = box
    for y in range(y0, y1):
        for x in range(x0, x1):
            f = 1.0 + (rnd.random() - 0.5) * 0.12
            # panel lines
            if (x - x0) % 4 == 0 and (y - y0) % 6 != 5:
                f *= 0.9
            img.putpixel((x, y), shade(pri, f) + (255,))
    # franja secundaria inferior y lateral
    for x in range(x0, x1):
        img.putpixel((x, y1 - 1), sec + (255,))


def armor_layers(h):
    hid, _, _, pri, sec, acc, _, emb = h
    seed = hash(hid) & 0xFFFF
    l1 = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    l2 = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    # Casco: cabeza (0,0)-(32,16)
    base_fill(l1, (0, 8, 32, 16), pri, sec, seed)
    base_fill(l1, (8, 0, 24, 8), pri, sec, seed + 1)
    # visor en la cara frontal (8..16, 8..16)
    d = ImageDraw.Draw(l1)
    d.rectangle([9, 11, 14, 12], fill=acc + (255,))
    d.rectangle([8, 8, 15, 8], fill=sec + (255,))
    # Peto: cuerpo (16,16)-(40,32) y brazos (40,16)-(56,32)
    base_fill(l1, (16, 20, 40, 32), pri, sec, seed + 2)
    base_fill(l1, (20, 16, 36, 20), pri, sec, seed + 3)
    base_fill(l1, (40, 20, 56, 32), pri, sec, seed + 4)
    base_fill(l1, (44, 16, 52, 20), pri, sec, seed + 5)
    draw_emblem(l1, emb, 20, 21, acc + (255,))  # frente del torso (20..28)
    d.rectangle([20, 29, 27, 29], fill=sec + (255,))
    d.rectangle([44, 20, 47, 21], fill=sec + (255,))
    d.rectangle([48, 20, 51, 21], fill=sec + (255,))
    # Botas: parte baja de las piernas en layer1 (0,16)-(16,32), solo filas 27..31
    base_fill(l1, (0, 27, 16, 32), shade(pri, 0.8), sec, seed + 6)
    d.rectangle([0, 27, 15, 27], fill=sec + (255,))
    base_fill(l1, (4, 16, 12, 20), shade(pri, 0.8), sec, seed + 7)
    # Pantalones layer2: piernas filas 20..27 + cinturon en cuerpo filas 20..23
    base_fill(l2, (0, 20, 16, 27), shade(pri, 0.9), sec, seed + 8)
    base_fill(l2, (4, 16, 12, 20), shade(pri, 0.9), sec, seed + 9)
    base_fill(l2, (16, 20, 40, 24), shade(pri, 0.9), sec, seed + 10)
    d2 = ImageDraw.Draw(l2)
    d2.rectangle([16, 20, 39, 20], fill=sec + (255,))
    d2.rectangle([22, 21, 25, 22], fill=acc + (255,))
    return l1, l2


# ---------- iconos 16x16 por mascara ----------
MASKS = {
    "helmet": [
        "................", "................", "....oooooooo....", "...o########o...",
        "..o##########o..", "..o##hh######o..", "..o##########o..", "..o##########o..",
        "..o#aaaaaaaa#o..", "..o#a#oooo#a#o..", "..ossoo..oossso.", "..oo........oo..",
        "................", "................", "................", "................"],
    "chestplate": [
        "................", "..ooo......ooo..", ".o###oo..oo###o.", ".o####oooo####o.",
        ".o#####oo#####o.", ".o##h#######h##o", ".oo##aa##aa###o.", "..o##a####a##o..",
        "..o#####aa####o.", "..o##########o..", "..o##########o..", "..ossssssssso...",
        "...oooooooooo...", "................", "................", "................"],
    "leggings": [
        "................", "...oooooooooo...", "..o##########o..", "..oasssaassas.o.",
        "..o##########o..", "..o#####oo####o.", "..o####o..o###o.", "..o###o....o##o.",
        "..o###o....o##o.", "..o###o....o##o.", "..o###o....o##o.", "..oss.o....oss.o",
        "..ooo.......ooo.", "................", "................", "................"],
    "boots": [
        "................", "................", "................", "................",
        "..ooo......ooo..", ".o###o....o###o.", ".o###o....o###o.", ".o###o....o###o.",
        ".o####o...o####o", ".o#####o.o#####o", "oo######oo######", "osssssssossssss.",
        "oooooooooooooooo", "................", "................", "................"],
    "serum": [
        "................", "..........ooo...", ".........o###o..", "..........ooo...",
        "........oo#o....", ".......o#oo.....", "......oFFFo.....", ".....oFFFFFo....",
        "....oFFFFFo.....", "...oFFFFFo......", "..oFFFFFo.......", ".oooFFFo........",
        "o#oooooo........", ".o..............", "................", "................"],
}


def icon_from_mask(mask, pri, sec, acc, fluid=None):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    outline = shade(pri, 0.35)
    for y, row in enumerate(mask):
        for x, ch in enumerate(row[:16]):
            if ch == "o":
                img.putpixel((x, y), outline + (255,))
            elif ch == "#":
                img.putpixel((x, y), shade(pri, 1.0 - 0.15 * (y % 2) + 0.05 * (x % 3)) + (255,))
            elif ch == "s":
                img.putpixel((x, y), sec + (255,))
            elif ch == "a":
                img.putpixel((x, y), acc + (255,))
            elif ch == "h":
                img.putpixel((x, y), shade(pri, 1.35) + (255,))
            elif ch == "F" and fluid:
                img.putpixel((x, y), fluid + (255,))
    return img


def stone(seed, size=16):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (size, size))
    for y in range(size):
        for x in range(size):
            v = 118 + rnd.randint(-14, 14)
            img.putpixel((x, y), (v, v, v + 2, 255))
    return img


def ore_texture():
    img = stone(7)
    rnd = random.Random(99)
    cols = [(40, 220, 230), (150, 70, 235), (230, 250, 255)]
    spots = [(3, 3), (10, 2), (6, 8), (12, 10), (3, 12), (9, 13)]
    for (cx, cy) in spots:
        for dx, dy in [(0, 0), (1, 0), (0, 1), (1, 1), (-1, 0)]:
            x, y = cx + dx, cy + dy
            if 0 <= x < 16 and 0 <= y < 16:
                img.putpixel((x, y), rnd.choice(cols) + (255,))
    return img


def table_textures():
    top = Image.new("RGBA", (16, 16), (50, 55, 70, 255))
    d = ImageDraw.Draw(top)
    d.rectangle([0, 0, 15, 15], outline=(25, 28, 38, 255))
    d.rectangle([2, 2, 13, 13], outline=(40, 220, 230, 255))
    for i in range(3, 13, 3):
        d.line([(i, 3), (i, 12)], fill=(90, 100, 125, 255))
        d.line([(3, i), (12, i)], fill=(90, 100, 125, 255))
    d.rectangle([6, 6, 9, 9], fill=(150, 70, 235, 255))
    side = Image.new("RGBA", (16, 16), (70, 76, 92, 255))
    d = ImageDraw.Draw(side)
    d.rectangle([0, 0, 15, 15], outline=(25, 28, 38, 255))
    d.rectangle([0, 0, 15, 3], fill=(40, 44, 58, 255))
    d.line([(1, 2), (14, 2)], fill=(40, 220, 230, 255))
    d.rectangle([3, 6, 12, 13], outline=(110, 118, 140, 255))
    d.rectangle([5, 8, 10, 11], fill=(150, 70, 235, 255))
    front = side.copy()
    ImageDraw.Draw(front).ellipse([6, 7, 9, 10], fill=(230, 250, 255, 255))
    bottom = Image.new("RGBA", (16, 16), (40, 44, 58, 255))
    return top, side, front, bottom


def material_icons():
    raw = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rnd = random.Random(3)
    d = ImageDraw.Draw(raw)
    d.polygon([(3, 12), (2, 7), (5, 3), (10, 2), (13, 6), (13, 11), (9, 13)], fill=(95, 98, 105, 255), outline=(45, 47, 55, 255))
    for (x, y) in [(5, 6), (9, 5), (8, 9), (11, 8), (5, 10)]:
        raw.putpixel((x, y), rnd.choice([(40, 220, 230, 255), (150, 70, 235, 255)]))
        raw.putpixel((x + 1, y), (230, 250, 255, 255))
    ingot = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(ingot)
    d.polygon([(2, 10), (5, 5), (14, 5), (11, 10)], fill=(60, 200, 220, 255), outline=(20, 70, 95, 255))
    d.polygon([(2, 10), (11, 10), (11, 12), (2, 12)], fill=(110, 60, 200, 255), outline=(40, 25, 90, 255))
    d.line([(5, 6), (12, 6)], fill=(230, 250, 255, 255))
    return raw, ingot


def save(img, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


def main():
    lang_es, lang_en = {}, {}
    tex = f"{A}/textures"

    # --- materiales y bloques ---
    save(ore_texture(), f"{tex}/block/hero_ore.png")
    t, s, f, b = table_textures()
    save(t, f"{tex}/block/hero_table_top.png")
    save(s, f"{tex}/block/hero_table_side.png")
    save(f, f"{tex}/block/hero_table_front.png")
    save(b, f"{tex}/block/hero_table_bottom.png")
    raw, ingot = material_icons()
    save(raw, f"{tex}/item/raw_heronite.png")
    save(ingot, f"{tex}/item/heronite_ingot.png")

    def item_model(name, parent="item/generated", tex_path=None):
        w(f"{A}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{name}"}})
        w(f"{A}/models/item/{name}.json", {"parent": parent, "textures": {"layer0": f"{NS}:item/{name}"}})

    item_model("raw_heronite")
    item_model("heronite_ingot")

    # bloques
    w(f"{A}/blockstates/hero_ore.json", {"variants": {"": {"model": f"{NS}:block/hero_ore"}}})
    w(f"{A}/models/block/hero_ore.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/hero_ore"}})
    w(f"{A}/items/hero_ore.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/hero_ore"}})
    w(f"{A}/blockstates/hero_table.json", {"variants": {"": {"model": f"{NS}:block/hero_table"}}})
    w(f"{A}/models/block/hero_table.json", {"parent": "minecraft:block/cube", "textures": {
        "particle": f"{NS}:block/hero_table_side", "down": f"{NS}:block/hero_table_bottom",
        "up": f"{NS}:block/hero_table_top", "north": f"{NS}:block/hero_table_front",
        "south": f"{NS}:block/hero_table_side", "east": f"{NS}:block/hero_table_side", "west": f"{NS}:block/hero_table_side"}})
    w(f"{A}/items/hero_table.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/hero_table"}})

    # loot, tags, worldgen
    w(f"{D}/loot_table/blocks/hero_ore.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{
        "type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": f"{NS}:hero_ore", "conditions": [{"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]},
            {"type": "minecraft:item", "name": f"{NS}:raw_heronite", "functions": [{"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"}, {"function": "minecraft:explosion_decay"}]}]}]}]})
    w(f"{D}/loot_table/blocks/hero_table.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:hero_table"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    w(f"{ROOT}/data/minecraft/tags/block/mineable/pickaxe.json", {"values": [f"{NS}:hero_ore", f"{NS}:hero_table"]})
    w(f"{ROOT}/data/minecraft/tags/block/needs_iron_tool.json", {"values": [f"{NS}:hero_ore"]})
    w(f"{D}/tags/item/repairs_hero_suit.json", {"values": [f"{NS}:heronite_ingot"]})
    w(f"{D}/worldgen/configured_feature/hero_ore.json", {"type": "minecraft:ore", "config": {
        "size": 6, "discard_chance_on_air_exposure": 0.25,
        "targets": [{"target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"}, "state": {"Name": f"{NS}:hero_ore"}},
                    {"target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"}, "state": {"Name": f"{NS}:hero_ore"}}]}})
    w(f"{D}/worldgen/placed_feature/hero_ore.json", {"feature": f"{NS}:hero_ore", "placement": [
        {"type": "minecraft:count", "count": 5}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:trapezoid", "min_inclusive": {"absolute": -60}, "max_inclusive": {"absolute": 20}}},
        {"type": "minecraft:biome"}]})

    # recetas base
    w(f"{D}/recipe/heronite_ingot_smelting.json", {"type": "minecraft:smelting", "category": "misc", "ingredient": f"{NS}:raw_heronite", "result": {"id": f"{NS}:heronite_ingot"}, "experience": 1.0, "cookingtime": 200})
    w(f"{D}/recipe/heronite_ingot_blasting.json", {"type": "minecraft:blasting", "category": "misc", "ingredient": f"{NS}:raw_heronite", "result": {"id": f"{NS}:heronite_ingot"}, "experience": 1.0, "cookingtime": 100})
    w(f"{D}/recipe/hero_table.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["HH", "PP"], "key": {"H": f"{NS}:heronite_ingot", "P": "#minecraft:planks"}, "result": {"id": f"{NS}:hero_table"}})

    lang_es.update({"block.heroforge.hero_ore": "Mena de Heronita", "block.heroforge.hero_table": "Mesa de Forja Heroica",
                    "item.heroforge.raw_heronite": "Heronita en Bruto", "item.heroforge.heronite_ingot": "Lingote de Heronita",
                    "itemGroup.heroforge": "HeroForge", "key.category.heroforge.main": "HeroForge",
                    "key.heroforge.power1": "Poder 1", "key.heroforge.power2": "Poder 2",
                    "msg.heroforge.serum_ok": "¡Poderes de %s despertados!", "msg.heroforge.no_serum": "Necesitas un suero heroico.",
                    "msg.heroforge.cooldown": "Poder en recarga (%s s)"})
    lang_en.update({"block.heroforge.hero_ore": "Heronite Ore", "block.heroforge.hero_table": "Hero Forge Table",
                    "item.heroforge.raw_heronite": "Raw Heronite", "item.heroforge.heronite_ingot": "Heronite Ingot",
                    "itemGroup.heroforge": "HeroForge", "key.category.heroforge.main": "HeroForge",
                    "key.heroforge.power1": "Power 1", "key.heroforge.power2": "Power 2",
                    "msg.heroforge.serum_ok": "%s powers awakened!", "msg.heroforge.no_serum": "You need a hero serum.",
                    "msg.heroforge.cooldown": "Power on cooldown (%s s)"})

    # --- heroes ---
    for h in HEROES:
        hid, es, en, pri, sec, acc, special, emb = h
        l1, l2 = armor_layers(h)
        save(l1, f"{tex}/entity/equipment/humanoid/{hid}.png")
        save(l2, f"{tex}/entity/equipment/humanoid_leggings/{hid}.png")
        w(f"{A}/equipment/{hid}.json", {"layers": {"humanoid": [{"texture": f"{NS}:{hid}"}], "humanoid_leggings": [{"texture": f"{NS}:{hid}"}]}})
        for p in PIECES:
            name = f"{hid}_{p}"
            save(icon_from_mask(MASKS[p], pri, sec, acc), f"{tex}/item/{name}.png")
            item_model(name)
            lang_es[f"item.{NS}.{name}"] = f"{PIECE_ES[p]} de {es}"
            lang_en[f"item.{NS}.{name}"] = f"{en} {PIECE_EN[p]}"
            pat = {"helmet": ["III", "I I"], "chestplate": ["I I", "III", "III"], "leggings": ["III", "I I", "I I"], "boots": ["I I", "I I"]}[p]
            key = {"I": f"{NS}:heronite_ingot"}
            if p == "chestplate":
                pat = ["I I", "ISI", "III"]
                key["S"] = special
            w(f"{D}/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pat, "key": key, "result": {"id": f"{NS}:{name}"}})
        sname = f"{hid}_serum"
        save(icon_from_mask(MASKS["serum"], (190, 200, 210), sec, acc, fluid=pri), f"{tex}/item/{sname}.png")
        item_model(sname)
        lang_es[f"item.{NS}.{sname}"] = f"Suero de {es}"
        lang_en[f"item.{NS}.{sname}"] = f"{en} Serum"
        lang_es[f"hero.{NS}.{hid}"] = es
        lang_en[f"hero.{NS}.{hid}"] = en
        w(f"{D}/recipe/{sname}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["ISI", "IBI", " G "],
            "key": {"I": f"{NS}:heronite_ingot", "S": special, "B": "minecraft:glass_bottle", "G": "minecraft:glistering_melon_slice"},
            "result": {"id": f"{NS}:{sname}"}})

    w(f"{A}/lang/es_es.json", lang_es)
    w(f"{A}/lang/en_us.json", lang_en)
    # icono del mod
    ic = Image.new("RGBA", (128, 128), (20, 22, 34, 255))
    d = ImageDraw.Draw(ic)
    d.rectangle([4, 4, 123, 123], outline=(40, 220, 230, 255), width=4)
    d.polygon([(64, 18), (104, 40), (104, 88), (64, 110), (24, 88), (24, 40)], fill=(110, 60, 200, 255), outline=(230, 250, 255, 255))
    d.polygon([(70, 30), (46, 68), (62, 68), (56, 98), (84, 56), (68, 56)], fill=(255, 235, 90, 255))
    save(ic, f"{A}/icon.png")
    print("OK", len(lang_es), "claves lang")


main()
