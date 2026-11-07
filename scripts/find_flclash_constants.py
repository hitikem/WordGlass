import re
from pathlib import Path

p = Path(r"D:\ds\WordGlass\flclash-extracted\lib\arm64-v8a\libapp.so")
data = p.read_bytes()

# search for the navigation dock implementation details
# look for float constants that match FlClash's known values
# _jellyAspect = 1.4, _lensGrowth = 8, _minLensWidth = 84, etc.

import struct

def find_float(fval, tol=1e-6):
    """Find float32 pattern in binary."""
    pat = struct.pack('<f', fval)
    results = []
    idx = 0
    while True:
        idx = data.find(pat, idx)
        if idx < 0:
            break
        results.append(idx)
        idx += 1
    return results

# FlClash constants
constants = {
    "_jellyAspect=1.4": 1.4,
    "_lensGrowth=8": 8.0,
    "_minLensWidth=84": 84.0,
    "_barHeight=62": 62.0,
    "_barPadding=4": 4.0,
    "_edgeMargin=21": 21.0,
    "_maxItemExtent=86": 86.0,
    "_iconSize=28": 28.0,
    "_labelSize=10": 10.0,
    "_labelInset=2": 2.0,
    "_minLabelSize=9": 9.0,
    "_pressGrowth=0.125": 0.125,
    "_maxPressGrowth=16": 16.0,
    "_liftSnap=0.975": 0.975,
    "_liftRelease=0.04": 0.04,
    "_hoverMagnet=0.2": 0.2,
    "_hoverParallax=0.5": 0.5,
    "_hoverAlpha=0.08": 0.08,
    "_hoverSwell=0.4": 0.4,
    "_jellySpeed=3": 3.0,
    "_jellyStretch=0.2": 0.2,
    "_jellyBrake=25": 25.0,
    "_jellySquash=0.15": 0.15,
    "_glowReach=28": 28.0,
    "_glowGrowth=3": 3.0,
    "_glowPeak=0.06": 0.06,
    "_glowWash=0.7": 0.7,
    "_overdrag=0.35": 0.35,
    "_pullLimit=7/32=0.21875": 0.21875,
    "_pullStretch=0.5": 0.5,
    "platter_alpha=0.85": 0.85,
    "tone_dark=4.0": 4.0,
    "tone_light=90.0": 90.0,
}

print("=== FlClash float constants in libapp.so ===")
for name, val in constants.items():
    positions = find_float(val)
    if positions:
        print(f"  {name:32s} found at {len(positions)} positions: {[f'0x{p:08x}' for p in positions[:3]]}")
    else:
        print(f"  {name:32s} NOT FOUND")

# search for string literals that reveal the rendering logic
print("\n=== String literals around _Lens / platter / jelly ===")
for term in [b"platter", b"jelly", b"_jelly", b"lensRect", b"lensHeight", b"lensWidth",
             b"stretch", b"squash", b"lift", b"AppShape", b"full", b"capsule",
             b"roundedRectangle", b"_GlassLens", b"_Lens", b"_LensPart"]:
    idx = 0
    while True:
        idx = data.find(term, idx)
        if idx < 0:
            break
        start = max(0, idx - 20)
        end = min(len(data), idx + len(term) + 40)
        chunk = data[start:end]
        printable = re.sub(rb'[^\x20-\x7e]', b'.', chunk)
        print(f"  {term.decode('latin1','replace'):24s} @0x{idx:08x}  {printable.decode('latin1')}")
        idx += 1
        break
