import struct, re
from pathlib import Path

p = Path(r"D:\ds\WordGlass\flclash-extracted\lib\arm64-v8a\libapp.so")
data = p.read_bytes()

# The string "platter" is at 0x000af26e. In Dart AOT, string literals are referenced
# by object pool offsets, and code objects are separate. But the .text section
# (ARM64 instructions) is where we need to look.
#
# Let's find the _Lens paint function. In FlClash source, _Lens.build() does:
#   - Hct.fromInt(secondary)
#   - Hct.from(hue, chroma, tone)
#   - Color(hct.toInt()).withValues(alpha: 0.85)
#   - PositionedDirectional(...)
#   - DecoratedBox(decoration: ShapeDecoration(color: platter..., shape: AppShape.full))
#
# The _Lens widget build is likely near the string "_Lens" (0x0005b165) or "_LensContent"
# Let's dump the AOT code objects near those strings.

# In Dart AOT, each function has a header with a pointer to its code (ARM64 instructions).
# The object pool contains entries that are 8-byte aligned.

# Let's search for ARM64 instruction patterns that indicate the lens drawing
#   - FMUL (floating multiply) for the jelly aspect scaling
#   - FADD/FSUB for the growth math
#   - LD1 (load 128-bit vector) for Color operations

# Instead of guessing, let's use a targeted approach:
# find the ARM64 code section, then find functions by their known entry points
# in the Dart object pool.

# Parse ELF to find .text section
import struct

def parse_elf(data):
    # ELF64 header
    e_shoff = struct.unpack_from('<Q', data, 40)[0]
    e_shentsize = struct.unpack_from('<H', data, 58)[0]
    e_shnum = struct.unpack_from('<H', data, 60)[0]
    e_shstrndx = struct.unpack_from('<H', data, 62)[0]
    print(f"ELF: shoff=0x{e_shoff:x} shentsize={e_shentsize} shnum={e_shnum}")
    
    # section header string table
    shstrtab_off = struct.unpack_from('<Q', data, e_shoff + e_shstrndx * e_shentsize + 24)[0]
    shstrtab = data[shstrtab_off:]
    
    for i in range(e_shnum):
        sh = e_shoff + i * e_shentsize
        sh_name = struct.unpack_from('<I', data, sh)[0]
        sh_type = struct.unpack_from('<I', data, sh+4)[0]
        sh_addr = struct.unpack_from('<Q', data, sh+16)[0]
        sh_offset = struct.unpack_from('<Q', data, sh+24)[0]
        sh_size = struct.unpack_from('<Q', data, sh+32)[0]
        # get name
        name_end = shstrtab[sh_name:].find(b'\x00')
        name = shstrtab[sh_name:sh_name+name_end].decode('latin1')
        if name in ('.text', '.rodata', '.data', '.data.rel.ro', '.bss'):
            print(f"  [{i:2d}] {name:20s} addr=0x{sh_addr:08x} off=0x{sh_offset:08x} size=0x{sh_size:08x} type={sh_type}")

parse_elf(data)
