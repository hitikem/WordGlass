import struct, re
from pathlib import Path

p = Path(r"D:\ds\WordGlass\flclash-extracted\lib\arm64-v8a\libapp.so")
data = p.read_bytes()
print(f"libapp.so size = {len(data)}")

# search for recognizable Dart symbols that relate to the navigation dock
# AOT snapshot preserves string literals and class/function names in the symbol table
search_terms = [
    b"NavigationDock",
    b"navigation_dock",
    b"NavigationDockDestination",
    b"_DockSurface",
    b"NavigationDockBar",
    b"DockSurface",
    b"NavigationDockItem",
    b"Bar",
    b"bottomNavigation",
    b"BottomNavigation",
    b"BottomBar",
    b"platter",
    b"Platter",
    b"liquid_glass",
    b"LiquidGlass",
    b"LiquidGlassStyle",
    b"AppShape",
    b"Hct",
    b"prism_ui",
    b"navigation_dock.dart",
    b"designSystem",
    b"design_system",
]
for term in search_terms:
    idx = 0
    while True:
        idx = data.find(term, idx)
        if idx < 0:
            break
        # extract surrounding printable bytes (string literal)
        start = max(0, idx - 40)
        end = min(len(data), idx + len(term) + 80)
        chunk = data[start:end]
        printable = re.sub(rb'[^\x20-\x7e]', b'.', chunk)
        print(f"  {term.decode('latin1','replace'):24s} @0x{idx:08x}  {printable.decode('latin1')}")
        idx += 1
        break  # first hit
