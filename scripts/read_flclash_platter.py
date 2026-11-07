from pathlib import Path
p = Path(r"C:\Users\Administrator\AppData\Local\Temp\prism_ui-src\lib\navigation_dock.dart")
src = p.read_text(encoding="utf-8")
lines = src.splitlines()
print(f"TOTAL LINES = {len(lines)}")
print("--- platter block (1440-1520) ---")
for i in range(1439, 1520):
    print(f"{i+1}: {lines[i]}")
print("--- lens rect + dock layout (113-155) ---")
for i in range(112, 155):
    print(f"{i+1}: {lines[i]}")
