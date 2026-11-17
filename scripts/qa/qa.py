#!/usr/bin/env python3
"""adb helpers for UI QA. Usage: qa.py <cmd> [args]

  shot NAME              save screenshot to docs/qa/out/NAME.png
  dump                   print visible nodes (text, desc, bounds) from uiautomator
  tap TEXT               tap the node whose text/content-desc equals TEXT
  type TEXT              type into the focused field
  back                   press back
  insets                 report content overlapping status/navigation bars
  edges                  report nodes with < 16dp horizontal margin
"""
import os, re, subprocess, sys, tempfile
import xml.etree.ElementTree as ET

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "docs", "qa", "out")


def adb(*args, binary=False):
    r = subprocess.run(["adb", *args], capture_output=True, check=True)
    return r.stdout if binary else r.stdout.decode().strip()


def density():
    m = re.search(r"(\d+)\s*$", adb("shell", "wm", "density").splitlines()[-1])
    return int(m.group(1)) / 160


def nodes():
    path = os.path.join(tempfile.gettempdir(), "qa_ui.xml")
    adb("shell", "uiautomator", "dump", "/sdcard/qa_ui.xml")
    adb("pull", "/sdcard/qa_ui.xml", path)
    for n in ET.parse(path).iter("node"):
        b = tuple(map(int, re.findall(r"\d+", n.get("bounds"))))
        yield n, b


def label(n):
    return n.get("text") or n.get("content-desc") or ""


def main():
    cmd, args = sys.argv[1], sys.argv[2:]
    if cmd == "shot":
        os.makedirs(OUT, exist_ok=True)
        p = os.path.join(OUT, f"{args[0]}.png")
        open(p, "wb").write(adb("exec-out", "screencap", "-p", binary=True))
        print(os.path.normpath(p))
    elif cmd == "dump":
        for n, b in nodes():
            if label(n):
                print(f"{label(n)!r:40} {b}")
    elif cmd == "tap":
        for n, b in nodes():
            if label(n) == args[0]:
                adb("shell", "input", "tap", str((b[0] + b[2]) // 2), str((b[1] + b[3]) // 2))
                print("tapped", args[0])
                return
        sys.exit(f"not found: {args[0]}")
    elif cmd == "type":
        adb("shell", "input", "text", args[0].replace(" ", "%s"))
    elif cmd == "back":
        adb("shell", "input", "keyevent", "4")
    elif cmd in ("insets", "edges"):
        d = density()
        w, h = map(int, re.findall(r"(\d+)x(\d+)", adb("shell", "wm", "size").splitlines()[-1])[0])
        status = int(24 * d)
        bad = []
        for n, b in nodes():
            if not label(n) or n.get("package") == "com.android.systemui":
                continue
            if cmd == "insets" and (b[1] < status or b[3] > h - int(16 * d)):
                bad.append(f"{label(n)!r} {b} touches system bar area")
            if cmd == "edges" and (b[0] < 16 * d or b[2] > w - 16 * d):
                bad.append(f"{label(n)!r} {b} < 16dp from screen edge")
        print("\n".join(bad) or "OK")
        sys.exit(1 if bad else 0)


main()
