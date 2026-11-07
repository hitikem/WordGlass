import struct, re
from pathlib import Path

p = Path(r"D:\ds\WordGlass\flclash-extracted\lib\arm64-v8a\libapp.so")
data = p.read_bytes()

def find_all(fval, fmt='<d', tol=1e-12):
    pat = struct.pack(fmt, fval)
    results = []
    idx = 0
    while True:
        idx = data.find(pat, idx)
        if idx < 0:
            break
        results.append(idx)
        idx += 1
    return results

# Search double64 for FlClash's key constants
# _jellyAspect = 1.4 (could be double64 in Dart AOT)
print("=== _jellyAspect = 1.4d (double64) ===")
for p_ in find_all(1.4, '<d')[:10]:
    print(f"  @0x{p_:08x}")

print("=== _jellyAspect = 1.4f (float32) ===")
for p_ in find_all(1.4, '<f')[:10]:
    print(f"  @0x{p_:08x}")

# _barHeight = 62.0
print("=== _barHeight = 62.0f ===")
for p_ in find_all(62.0, '<f')[:5]:
    print(f"  @0x{p_:08x}")

# _minLensWidth = 84.0
print("=== _minLensWidth = 84.0f ===")
for p_ in find_all(84.0, '<f')[:5]:
    print(f"  @0x{p_:08x}")

# _barPadding = 4.0
print("=== _barPadding = 4.0f ===")
for p_ in find_all(4.0, '<f')[:5]:
    print(f"  @0x{p_:08x}")

# _edgeMargin = 21.0
print("=== _edgeMargin = 21.0f ===")
for p_ in find_all(21.0, '<f')[:5]:
    print(f"  @0x{p_:08x}")

# platter alpha = 0.85
print("=== platter_alpha = 0.85f ===")
for p_ in find_all(0.85, '<f')[:5]:
    print(f"  @0x{p_:08x}")

# _jellyStretch = 0.2
print("=== _jellyStretch = 0.2f ===")
for p_ in find_all(0.2, '<f')[:5]:
    print(f"  @0x{p_:08x}")

# _jellySquash = 0.15
print("=== _jellySquash = 0.15f ===")
for p_ in find_all(0.15, '<f')[:5]:
    print(f"  @0x{p_:08x}")

# tone_light = 90.0
print("=== tone_light = 90.0f ===")
for p_ in find_all(90.0, '<f')[:5]:
    print(f"  @0x{p_:08x}")

# tone_dark = 4.0
print("=== tone_dark = 4.0f ===")
for p_ in find_all(4.0, '<f')[:5]:
    print(f"  @0x{p_:08x}")
