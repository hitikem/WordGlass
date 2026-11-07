from pathlib import Path
import os
# search for FlClash artifacts in common locations
search_dirs = [
    Path(r"C:\Users\Administrator\AppData\Local\Temp"),
    Path(r"C:\Users\Administrator\Downloads"),
    Path(r"C:\Users\Administrator\Desktop"),
    Path(r"D:\ds"),
]
for d in search_dirs:
    if not d.exists():
        continue
    print(f"\n=== {d} ===")
    for p in d.rglob("*"):
        if p.is_file() and (
            "flclash" in p.name.lower() or
            "fl_clash" in p.name.lower() or
            p.suffix.lower() in {".apk", ".asar", ".aab"}
        ):
            try:
                size = p.stat().st_size
                print(f"  {p}  ({size} bytes)")
            except Exception as e:
                print(f"  {p}  (error: {e})")
