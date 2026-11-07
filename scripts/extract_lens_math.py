import struct, re
from pathlib import Path

p = Path(r"D:\ds\WordGlass\flclash-extracted\lib\arm64-v8a\libapp.so")
data = p.read_bytes()

# The navigation dock Flutter AOT code has specific float sequences
# FlClash's _lensRect formula:
#   lensWidth  = (width + growth) * (1 + stretch)
#   lensHeight = (height + growth) * (1 - stretch * _jellyAspect)
# where growth = _lensGrowth * 2 * lift
# 
# Look for ARM64 float constants near the platter/lens/jelly function.
# In ARM64 AOT, Dart floats are loaded via `ldr sX, [pc, #imm]` or `fmov`.
# We can search for the specific float bit patterns.

# FlClash's _jellyAspect = 1.4 (float32)
# _lensGrowth = 8.0
# _barHeight = 62.0
# _minLensWidth = 84.0
# _barPadding = 4.0
# platter alpha = 0.85

def find_all_float(fval, tol=1e-7):
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

# Search for float constants that ARE unique to the lens/jelly math
# _jellyAspect = 1.4f
print("=== _jellyAspect = 1.4f ===")
for p_ in find_all_float(1.4):
    print(f"  @0x{p_:08x}")

# _lensGrowth = 8.0f (already found)
print("=== _lensGrowth = 8.0f ===")
for p_ in find_all_float(8.0)[:5]:
    print(f"  @0x{p_:08x}")

# Look at the code around 0x000f253a (lensGrowth=8) and 0x0057a054
# These are likely in the _lensRect / platter functions
# dump surrounding code
for center in [0x000f253a, 0x0057a054]:
    print(f"\n=== bytes around 0x{center:08x} ===")
    start = max(0, center - 64)
    end = min(len(data), center + 64)
    chunk = data[start:end]
    # show as hex
    for i in range(0, len(chunk), 16):
        h = ' '.join(f'{b:02x}' for b in chunk[i:i+16])
        print(f"  {start+i:08x}: {h}")

# The AOT code for NavigationDock is likely near the string refs
# Find the string "navigation_dock.dart" address and look for nearby code
nav_dock_str = data.find(b"package:prism_ui/navigation_dock.dart")
print(f"\n=== navigation_dock.dart string at 0x{nav_dock_str:08x} ===")

# Find the string "platter" and dump nearby code
platter_str = data.find(b"platter")
print(f"=== platter string at 0x{platter_str:08x} ===")
start = max(0, platter_str - 32)
end = min(len(data), platter_str + 32)
chunk = data[start:end]
for i in range(0, len(chunk), 16):
    h = ' '.join(f'{b:02x}' for b in chunk[i:i+16])
    print(f"  {start+i:08x}: {h}")

# Search for the _lensRect formula constants
# (width + growth) * (1 + stretch)
# (height + growth) * (1 - stretch * _jellyAspect)
# Look for float 1.0f, 1.4f near each other (multiply pattern)
print("\n=== searching for float 1.0f near 1.4f (jelly aspect multiply) ===")
ones = find_all_float(1.0)
aspects = find_all_float(1.4)
for a in aspects:
    for o in ones:
        if abs(a - o) < 16:
            print(f"  1.4f@0x{a:08x}  1.0f@0x{o:08x}  dist={abs(a-o)}")
