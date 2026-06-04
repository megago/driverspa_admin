from PIL import Image, ImageDraw, ImageFont, ImageFilter
import os

OUT = os.path.dirname(os.path.abspath(__file__))
ICON_SRC = os.path.join(os.path.dirname(OUT),
    "app", "src", "main", "res", "mipmap-xxxhdpi", "ic_launcher.png")

# ── 1. App icon 512×512 ───────────────────────────────────────────────
icon_src = Image.open(ICON_SRC).convert("RGBA")

# Scale up with high-quality resampling
icon_512 = icon_src.resize((512, 512), Image.LANCZOS)

icon_512.save(os.path.join(OUT, "ic_launcher_512.png"))
print("Saved ic_launcher_512.png  (512×512)")

# ── 2. Feature graphic 1024×500 ──────────────────────────────────────
W, H = 1024, 500

# Background gradient from dark navy to brand blue
feat = Image.new("RGB", (W, H))
draw = ImageDraw.Draw(feat)

# Draw vertical gradient bands
for x in range(W):
    t = x / W  # 0 → 1  (left → right)
    r = int(20  + t * (37  - 20))   # 14 → 25
    g = int(30  + t * (99  - 30))   # 1e → 63
    b = int(60  + t * (181 - 60))   # 3c → b5
    draw.line([(x, 0), (x, H)], fill=(r, g, b))

# Paste the icon (centered-left area, large)
icon_feat = icon_src.resize((260, 260), Image.LANCZOS)
icon_x = 80
icon_y = (H - 260) // 2
feat.paste(icon_feat, (icon_x, icon_y), icon_feat)

# Try to load a font; fall back to default if not available
def load_font(size):
    for path in [
        "C:/Windows/Fonts/arialbd.ttf",
        "C:/Windows/Fonts/arial.ttf",
        "C:/Windows/Fonts/calibrib.ttf",
    ]:
        try:
            return ImageFont.truetype(path, size)
        except:
            pass
    return ImageFont.load_default()

font_large  = load_font(72)
font_medium = load_font(34)
font_small  = load_font(26)

text_x = icon_x + 260 + 60   # to the right of the icon

# App name
draw.text((text_x, 130), "DriverSpa", font=font_large, fill=(255, 255, 255))

# Tagline (admin)
draw.text((text_x, 220), "Admin Panel", font=font_medium, fill=(180, 210, 255))

# Three bullet points
bullets = [
    "Manage bookings & queues",
    "Real-time timetable per box",
    "Clients, payments & analytics",
]
y = 280
for b in bullets:
    draw.text((text_x, y), "•  " + b, font=font_small, fill=(200, 225, 255))
    y += 42

feat.save(os.path.join(OUT, "feature_graphic_1024x500.png"))
print("Saved feature_graphic_1024x500.png  (1024×500)")

# ── 3. Feature graphic RU 1024×500 ───────────────────────────────────
feat_ru = feat.copy()
draw_ru = ImageDraw.Draw(feat_ru)

# Wipe the text area with a gradient repaint (same gradient, right half)
for x in range(text_x - 10, W):
    t = x / W
    r = int(20  + t * (37  - 20))
    g = int(30  + t * (99  - 30))
    b = int(60  + t * (181 - 60))
    draw_ru.line([(x, 0), (x, H)], fill=(r, g, b))

# Try Cyrillic-capable fonts first
def load_font_ru(size):
    for path in [
        "C:/Windows/Fonts/arialbd.ttf",
        "C:/Windows/Fonts/arial.ttf",
        "C:/Windows/Fonts/calibrib.ttf",
        "C:/Windows/Fonts/segoeui.ttf",
        "C:/Windows/Fonts/tahoma.ttf",
    ]:
        try:
            return ImageFont.truetype(path, size)
        except:
            pass
    return ImageFont.load_default()

font_large_ru  = load_font_ru(72)
font_medium_ru = load_font_ru(34)
font_small_ru  = load_font_ru(26)

draw_ru.text((text_x, 130), "DriverSpa",         font=font_large_ru,  fill=(255, 255, 255))
draw_ru.text((text_x, 220), "Панель администратора", font=font_medium_ru, fill=(180, 210, 255))

bullets_ru = [
    "Управление бронями и очередью",
    "Расписание боксов в реальном времени",
    "Клиенты, оплата и аналитика",
]
y = 280
for b in bullets_ru:
    draw_ru.text((text_x, y), "•  " + b, font=font_small_ru, fill=(200, 225, 255))
    y += 42

feat_ru.save(os.path.join(OUT, "feature_graphic_ru_1024x500.png"))
print("Saved feature_graphic_ru_1024x500.png  (1024×500)")
