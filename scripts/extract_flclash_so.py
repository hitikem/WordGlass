import zipfile
import os
apk_path = r"D:\ds\flclash\FlClash-0.9.3-android-arm64-v8a.apk"
out_dir = r"D:\ds\WordGlass\flclash-extracted"
os.makedirs(out_dir, exist_ok=True)
with zipfile.ZipFile(apk_path, 'r') as z:
    for name in z.namelist():
        if name.endswith('.so') and ('app' in name.lower() or 'flutter' in name.lower()):
            z.extract(name, out_dir)
            print(f"extracted: {name}")
    # list all .so
    print("\n=== all .so ===")
    for name in z.namelist():
        if name.endswith('.so'):
            print(f"  {name}")
