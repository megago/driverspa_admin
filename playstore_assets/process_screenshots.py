from PIL import Image
import os, shutil

TEMP = os.path.expandvars(r"%TEMP%")
OUT  = os.path.dirname(os.path.abspath(__file__))
TARGET_W, TARGET_H = 1080, 1920

screenshots = [
    ("sc_history.png",    "01_history"),
    ("sc_drawer.png",     "02_menu"),
    ("s2.png",            "03_timetable"),
    ("sc_clients.png",    "04_clients"),
    ("sc_profile.png",    "05_settings"),
    ("sc_orders.png",     "06_orders_today"),
]

for src_name, label in screenshots:
    src = os.path.join(TEMP, src_name)
    if not os.path.exists(src):
        print(f"SKIP (not found): {src_name}")
        continue

    img = Image.open(src).convert("RGB")
    w, h = img.size

    # Upscale keeping aspect ratio, then pad to exact 1080x1920
    scale = min(TARGET_W / w, TARGET_H / h)
    new_w = int(w * scale)
    new_h = int(h * scale)
    img = img.resize((new_w, new_h), Image.LANCZOS)

    # Pad with matching edge color (dark background)
    canvas = Image.new("RGB", (TARGET_W, TARGET_H), (18, 26, 42))
    x = (TARGET_W - new_w) // 2
    y = (TARGET_H - new_h) // 2
    canvas.paste(img, (x, y))

    out_path = os.path.join(OUT, f"screenshot_phone_{label}.png")
    canvas.save(out_path, "PNG")
    print(f"Saved {os.path.basename(out_path)}  ({TARGET_W}x{TARGET_H})")

print("\nDone — all screenshots in:", OUT)
