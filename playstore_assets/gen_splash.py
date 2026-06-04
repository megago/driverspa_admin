from PIL import Image, ImageDraw, ImageFont
import os, math

OUT = os.path.dirname(os.path.abspath(__file__))
W, H = 1080, 1920

img = Image.new("RGB", (W, H))
draw = ImageDraw.Draw(img)

# Dark gradient background (top-left navy → bottom-right deeper navy, matching gradient_bg)
for y in range(H):
    t = y / H
    r = int(20  + t * 5)
    g = int(28  + t * 8)
    b = int(52  + t * 20)
    draw.line([(0, y), (W, y)], fill=(r, g, b))

cx, cy = W // 2, H // 2

# Three concentric semi-transparent circles (120dp, 220dp, 320dp — at 3x = 360, 660, 960 px)
for radius, alpha in [(480, 18), (330, 25), (180, 35)]:
    circle = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    cd = ImageDraw.Draw(circle)
    cd.ellipse(
        [cx - radius, cy - radius, cx + radius, cy + radius],
        outline=(255, 255, 255, alpha),
        width=2
    )
    img.paste(Image.alpha_composite(img.convert("RGBA"), circle).convert("RGB"))

# App icon centered (200×200)
icon_path = os.path.join(OUT, "ic_launcher_512.png")
icon = Image.open(icon_path).convert("RGBA").resize((200, 200), Image.LANCZOS)
img.paste(icon, (cx - 100, cy - 140), icon)

# "DriverSpa" bold text below icon
def font(size):
    for p in ["C:/Windows/Fonts/arialbd.ttf", "C:/Windows/Fonts/calibrib.ttf"]:
        try: return ImageFont.truetype(p, size)
        except: pass
    return ImageFont.load_default()

f_title = font(72)
f_copy  = font(30)

# Title
bbox = draw.textbbox((0, 0), "DriverSpa", font=f_title)
tw = bbox[2] - bbox[0]
draw.text((cx - tw // 2, cy + 85), "DriverSpa", font=f_title, fill=(255, 255, 255))

# Copyright
bbox2 = draw.textbbox((0, 0), "© 2026 DriverSpa", font=f_copy)
cw = bbox2[2] - bbox2[0]
draw.text((cx - cw // 2, H - 100), "© 2026 DriverSpa", font=f_copy, fill=(255, 255, 255, 180))

out_path = os.path.join(OUT, "screenshot_phone_00_splash.png")
img.save(out_path)
print(f"Saved {out_path}  ({W}x{H})")
