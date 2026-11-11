"""Generates src/main/resources/assets/donutsell/icon.png in the Polyfrost / OneClient icon style:
light blue rounded tile (#ECF2FF), thick round-capped blue line glyph with a top-to-bottom
gradient (#0957DF -> #0444B3) and a lighter detached accent (#3871D4).
Run: python3 art/make_icon.py  (needs Pillow)"""
import math
from pathlib import Path
from PIL import Image, ImageDraw

S = 1024            # draw size (supersampled), downscaled at the end
OUT = 256
BG = (236, 242, 255, 255)
TOP, BOTTOM = (9, 87, 223), (4, 68, 179)
ACCENT = (56, 113, 212, 255)
W = int(S * 0.066)  # stroke width


def rounded_tile():
    tile = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    ImageDraw.Draw(tile).rounded_rectangle((0, 0, S - 1, S - 1), radius=int(S * 0.22), fill=BG)
    return tile


def arc(draw, cx, cy, r, start, end, width, fill):
    """Arc with round caps; angles in degrees, clockwise from 3 o'clock (PIL convention)."""
    draw.arc((cx - r, cy - r, cx + r, cy + r), start, end, fill=fill, width=width)
    rm = r - width / 2
    for a in (start, end):
        x, y = cx + rm * math.cos(math.radians(a)), cy + rm * math.sin(math.radians(a))
        draw.ellipse((x - width / 2, y - width / 2, x + width / 2, y + width / 2), fill=fill)


def line(draw, x1, y1, x2, y2, width, fill):
    draw.line((x1, y1, x2, y2), fill=fill, width=width)
    for x, y in ((x1, y1), (x2, y2)):
        draw.ellipse((x - width / 2, y - width / 2, x + width / 2, y + width / 2), fill=fill)


def main():
    img = rounded_tile()

    # --- main glyph (mask, then filled with the vertical gradient)
    mask = Image.new("L", (S, S), 0)
    d = ImageDraw.Draw(mask)
    cx, cy = S * 0.46, S * 0.46
    # donut outer edge, open towards the bottom right where the coin sits
    arc(d, cx, cy, S * 0.31, 75, 375, W, 255)
    # donut hole
    arc(d, cx, cy, S * 0.105, 0, 360, W, 255)
    # sprinkles on the icing
    for ang, length in ((215, 0.07), (285, 0.07), (150, 0.06)):
        a = math.radians(ang)
        r = S * 0.205
        x, y = cx + r * math.cos(a), cy + r * math.sin(a)
        t = a + math.pi / 2.6
        dx, dy = math.cos(t) * S * length / 2, math.sin(t) * S * length / 2
        line(d, x - dx, y - dy, x + dx, y + dy, int(W * 0.8), 255)

    grad = Image.new("RGBA", (S, S))
    gd = ImageDraw.Draw(grad)
    for y in range(S):
        k = y / (S - 1)
        gd.line((0, y, S, y), fill=tuple(int(TOP[i] + (BOTTOM[i] - TOP[i]) * k) for i in range(3)) + (255,))
    img.paste(grad, (0, 0), mask)

    # --- detached accent: a coin with a $ bar, sitting in the gap of the donut
    acc = ImageDraw.Draw(img)
    kx, ky, kr = S * 0.725, S * 0.725, S * 0.125
    arc(acc, kx, ky, kr, 0, 360, int(W * 0.85), ACCENT)
    line(acc, kx, ky - kr * 0.42, kx, ky + kr * 0.42, int(W * 0.7), ACCENT)

    out = Path(__file__).resolve().parent.parent / "src/main/resources/assets/donutsell/icon.png"
    img.resize((OUT, OUT), Image.LANCZOS).save(out)
    print("wrote", out)


if __name__ == "__main__":
    main()
