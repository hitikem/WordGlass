from PIL import Image
import numpy as np, os

mask = Image.open(r'D:\ds\WordGlass\build-w-mask.png').convert('L')
mw, mh = mask.size
# Remove the decorative corner flourishes: keep only the central W strokes.
arr = np.array(mask)
keep = np.zeros_like(arr)
# The W occupies roughly the middle 60% horizontally.
x0, x1 = int(mw * 0.20), int(mw * 0.80)
keep[:, x0:x1] = arr[:, x0:x1]
mask = Image.fromarray(keep)
bbox = mask.getbbox()
mask = mask.crop(bbox)
mw, mh = mask.size
print('final glyph', mw, mh)

PAPER = (247, 246, 242)
INK = (32, 33, 31)

def make(size, bg, fg, pad_ratio, round_mask=False, out=None):
    canvas = Image.new('RGBA', (size, size), bg + (255,))
    target_w = int(size * (1 - 2 * pad_ratio))
    scale = target_w / mw
    tw, th = int(mw * scale), int(mh * scale)
    if th > int(size * (1 - 2 * pad_ratio)):
        scale = int(size * (1 - 2 * pad_ratio)) / mh
        tw, th = int(mw * scale), int(mh * scale)
    g = mask.resize((tw, th), Image.LANCZOS)
    glyph = Image.new('RGBA', (tw, th), fg + (0,))
    alpha = g.point(lambda v: v)
    glyph.putalpha(alpha)
    canvas.alpha_composite(glyph, ((size - tw) // 2, (size - th) // 2))
    if round_mask:
        m = Image.new('L', (size, size), 0)
        from PIL import ImageDraw
        ImageDraw.Draw(m).ellipse((0, 0, size - 1, size - 1), fill=255)
        canvas.putalpha(m)
    if out:
        canvas.save(out)
        print('wrote', out, canvas.size)

res = r'D:\ds\WordGlass\app\src\main\res'
dens = {'mdpi': 48, 'hdpi': 72, 'xhdpi': 96, 'xxhdpi': 144, 'xxxhdpi': 192}
for name, px in dens.items():
    d = os.path.join(res, 'mipmap-' + name)
    os.makedirs(d, exist_ok=True)
    make(px, PAPER, INK, 0.20, False, os.path.join(d, 'ic_launcher.png'))
    make(px, PAPER, INK, 0.20, True, os.path.join(d, 'ic_launcher_round.png'))
