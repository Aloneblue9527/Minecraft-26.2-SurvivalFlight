#!/usr/bin/env python3
"""Check the release archive. Static checks support, but do not replace, source review."""
import hashlib
import json
import struct
import sys
import zipfile
from pathlib import Path

version = next(line.split("=", 1)[1].strip() for line in Path("gradle.properties").read_text().splitlines() if line.startswith("mod_version="))
jar = Path(sys.argv[1] if len(sys.argv) > 1 else f"build/libs/survivalflight-26.2-{version}.jar")
with zipfile.ZipFile(jar) as archive:
    names = archive.namelist()
    assert len(names) == len(set(names)), "Duplicate ZIP entries"
    files = {n for n in names if not n.endswith("/")}
    expected = {
        "META-INF/MANIFEST.MF", "LICENSE", "fabric.mod.json", "survivalflight.mixins.json",
        "assets/survivalflight/lang/en_us.json", "assets/survivalflight/lang/zh_cn.json",
        "dev/survivalflight/FlightSession.class", "dev/survivalflight/FlightSession$FlightFlags.class",
        "dev/survivalflight/SurvivalFlightClient.class", "dev/survivalflight/SurvivalFlightClient$1.class",
        "dev/survivalflight/mixin/ClientPacketListenerMixin.class",
        "dev/survivalflight/FlightSafety.class",
        "dev/survivalflight/mixin/PlayerMiningMixin.class",
        "dev/survivalflight/mixin/MovePlayerPacketMixin.class",
        "dev/survivalflight/mixin/MiningPacketMixin.class",
    }
    assert files == expected, f"Unexpected or missing files: {files ^ expected}"
    metadata = json.loads(archive.read("fabric.mod.json"))
    assert metadata["id"] == "survivalflight"
    assert metadata["version"] == version and metadata["environment"] == "client"
    assert metadata["depends"]["minecraft"] == "26.2"
    assert metadata["license"] == "MIT"
    assert metadata["mixins"] == ["survivalflight.mixins.json"]
    mixins = json.loads(archive.read("survivalflight.mixins.json"))
    assert mixins["required"] and mixins["injectors"]["defaultRequire"] == 1
    assert mixins["client"] == ["ClientPacketListenerMixin", "PlayerMiningMixin", "MovePlayerPacketMixin", "MiningPacketMixin"]
    for name in files:
        data = archive.read(name)
        if name.endswith(".class"):
            assert data[:4] == bytes.fromhex("cafebabe")
            assert struct.unpack(">H", data[6:8])[0] == 69, "Expected Java 25 bytecode"
            if b"ServerboundMovePlayerPacket" in data:
                assert name in {"dev/survivalflight/mixin/MovePlayerPacketMixin.class", "dev/survivalflight/mixin/MiningPacketMixin.class", "dev/survivalflight/SurvivalFlightClient.class"}
            for forbidden in (
                b"java/lang/Runtime", b"java/lang/ProcessBuilder", b"java/net/",
                b"java/nio/file/", b"java/io/", b"sun/misc/Unsafe", b"loadLibrary",
                b"fallDistance", b"setFlyingSpeed",
                b"setDeltaMovement", b"noPhysics",
            ):
                assert forbidden not in data, f"Review unexpected API {forbidden!r} in {name}"
    for language in ("en_us", "zh_cn"):
        translations = json.loads(archive.read(f"assets/survivalflight/lang/{language}.json"))
        assert set(translations) == {
            "key.survivalflight.toggle", "message.survivalflight.on",
            "message.survivalflight.off", "message.survivalflight.danger",
        }
digest = hashlib.sha256(jar.read_bytes()).hexdigest()
out = Path("build/distributions")
out.mkdir(parents=True, exist_ok=True)
(out / "SHA256SUMS").write_text(f"{digest}  {jar.name}\n", encoding="ascii")
(out / "jar-contents.txt").write_text("\n".join(names) + "\n", encoding="utf-8")
print(f"JAR inspection passed: {len(files)} files; Java 25; client-only; no bundled libraries/native code")
print(f"SHA-256: {digest}")
