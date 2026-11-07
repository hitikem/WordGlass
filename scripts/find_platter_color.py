import struct, re
from pathlib import Path

p = Path(r"D:\ds\WordGlass\flclash-extracted\lib\arm64-v8a\libapp.so")
data = p.read_bytes()

# Find the _Lens build function code in .text
# In Dart AOT, code objects are in .text (0x5a0000-0x5a0000+0x96d760)
# Each function object has a code pointer in the object pool
#
# We can find the code by looking for the ARM64 instruction sequence
# that implements the platter rendering:
#   - Color(hct.toInt()).withValues(alpha: 0.85)
#   - DecoratedBox(decoration: ShapeDecoration(color: platter..., shape: AppShape.full))
#
# The key is finding where 0.85 (platter alpha) is encoded as an ARM64 immediate.
# ARM64 FMOV s0, #0.85 is NOT a valid immediate, so 0.85 must be loaded from
# a literal pool or computed.
#
# In Flutter AOT, Dart's Color.withValues(alpha: 0.85) becomes a Color constructor
# with the alpha value baked in. The Color class stores alpha as 0..1 float, but
# when converted to ARGB32 it becomes an integer (0.85 * 255 = 217 = 0xD9).
#
# Let's search for 0xD9 (217) in the .rodata near "platter" to find the color literal.

platter_str_pos = data.find(b"platter")
print(f"platter string at 0x{platter_str_pos:08x}")

# Search around for 0xD9 (217 = 0.85 * 255)
for i in range(max(0, platter_str_pos - 256), min(len(data), platter_str_pos + 256)):
    if data[i] == 0xD9:
        # show surrounding bytes
        start = max(0, i - 16)
        end = min(len(data), i + 16)
        chunk = data[start:end]
        printable = re.sub(rb'[^\x20-\x7e]', b'.', chunk)
        print(f"  0xD9 @0x{i:08x}  {printable.decode('latin1')}")

# Also search for 0x41 (65 = 0.255 * 255 = alpha 25% for glass)
print("\n=== search for 0.25*255=64 (glass tint alpha) ===")
for i in range(max(0, platter_str_pos - 256), min(len(data), platter_str_pos + 256)):
    if data[i] == 64:
        start = max(0, i - 16)
        end = min(len(data), i + 16)
        chunk = data[start:end]
        printable = re.sub(rb'[^\x20-\x7e]', b'.', chunk)
        print(f"  64 @0x{i:08x}  {printable.decode('latin1')}")

# Search for the Color class "withValues" method reference
print("\n=== search for 'withValues' ===")
idx = 0
while True:
    idx = data.find(b"withValues", idx)
    if idx < 0:
        break
    start = max(0, idx - 20)
    end = min(len(data), idx + 20)
    chunk = data[start:end]
    printable = re.sub(rb'[^\x20-\x7e]', b'.', chunk)
    print(f"  @0x{idx:08x}  {printable.decode('latin1')}")
    idx += 1
    if idx > 10:
        break

# Search for "alpha" in .rodata (Dart's Color.withValues(alpha: ...))
print("\n=== search for 'alpha:' ===")
idx = 0
count = 0
while count < 5:
    idx = data.find(b"alpha:", idx)
    if idx < 0:
        break
    start = max(0, idx - 16)
    end = min(len(data), idx + 32)
    chunk = data[start:end]
    printable = re.sub(rb'[^\x20-\x7e]', b'.', chunk)
    print(f"  @0x{idx:08x}  {printable.decode('latin1')}")
    idx += 1
    count += 1
