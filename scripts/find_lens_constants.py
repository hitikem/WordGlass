import struct, re
from pathlib import Path

p = Path(r"D:\ds\WordGlass\flclash-extracted\lib\arm64-v8a\libapp.so")
data = p.read_bytes()

# The _lensRect formula in FlClash is:
#   growth = _lensGrowth * 2 * lift  (= 8 * 2 * lift = 16 * lift)
#   lensWidth = (width + growth) * (1 + stretch)
#   lensHeight = (height + growth) * (1 - stretch * _jellyAspect)
#
# In ARM64 AOT, this compiles to a sequence like:
#   FMOV s0, #16.0        (growth multiplier = 2*8 = 16)
#   FMUL s0, s0, s1       (16 * lift)
#   FADD s2, s2, s0       (width + growth)
#   FMOV s3, #1.0
#   FADD s3, s3, s4       (1 + stretch)
#   FMUL s2, s2, s3       (width+growth)*(1+stretch)  => lensWidth
#   ...
#   FMOV s5, #1.4         (jellyAspect)
#   FMUL s5, s4, s5       (stretch * jellyAspect)
#   FMOV s6, #1.0
#   FSUB s6, s6, s5       (1 - stretch*aspect)
#   FMUL s3, s3, s6       ((height+growth)*(1-stretch*aspect)) => lensHeight

# Search for ARM64 FMOV immediate for 16.0
# FMOV sD, #imm uses encoding: 0001 1110 0010 0000 0000 10xx xxxx xxxx
# FMOV s0, #16.0  = 0x1E22 1000  (approximate)

# Better: search for the literal 16.0f (0x41800000) which is the growth multiplier
def find_all(fval, fmt='<f'):
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

print("=== 16.0f (growth = 2*8 = 16) ===")
for p_ in find_all(16.0)[:5]:
    print(f"  @0x{p_:08x}")

# Search for 1.0f (used in 1+stretch and 1-stretch*aspect)
print("=== 1.0f ===")
for p_ in find_all(1.0)[:5]:
    print(f"  @0x{p_:08x}")

# Search for 0.85f (platter alpha)
print("=== 0.85f ===")
for p_ in find_all(0.85)[:5]:
    print(f"  @0x{p_:08x}")

# Search for 0.2f (jellyStretch)
print("=== 0.2f ===")
for p_ in find_all(0.2)[:5]:
    print(f"  @0x{p_:08x}")

# 0.15f (jellySquash)
print("=== 0.15f ===")
for p_ in find_all(0.15)[:5]:
    print(f"  @0x{p_:08x}")

# 8.0f (lensGrowth)
print("=== 8.0f ===")
for p_ in find_all(8.0)[:5]:
    print(f"  @0x{p_:08x}")

# 62.0f (barHeight) - already found
print("=== 62.0f ===")
for p_ in find_all(62.0)[:5]:
    print(f"  @0x{p_:08x}")

# 84.0f (minLensWidth)
print("=== 84.0f ===")
for p_ in find_all(84.0)[:5]:
    print(f"  @0x{p_:08x}")
