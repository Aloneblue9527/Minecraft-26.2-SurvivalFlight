# SurvivalFlight

Experimental client-side building flight for Minecraft 26.2 / Fabric. Use only in private worlds with the host's permission.

## Usage
Install matching Fabric Loader and Fabric API, then put the built JAR in your client's mods folder. F7 toggles flight permission; double-tap jump to fly, jump to ascend and sneak to descend. No speed boost, noclip or unrelated modules. Flight resets on world changes and death.

Experimental anti-kick and fall protection are not guaranteed: the server controls movement and damage. Test at low altitude in a backup world. Never rely on fall protection when disconnecting or disabling flight at height.

## Build
Java 25 and Gradle 9.5.1. Run `gradle build`. Alternatively use GitHub Actions and download the successful build artifact. No local development environment needed.

Status: source preview; runtime and multiplayer compatibility not yet verified. No antivirus certification. MIT license.
