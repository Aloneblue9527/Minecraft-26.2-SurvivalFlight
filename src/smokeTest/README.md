# Disposable integration fixture

This is a separate TEST MOD. It is excluded from the installable SurvivalFlight JAR and normal build output. Only run it in a disposable single-player world: it places/removes blocks, teleports, changes difficulty, initializes server health and intentionally dies in all three vanilla dimensions.

1. In the cloud, run `./gradlew clean build smokeTestJar` with Java 25.
2. Create a separate game directory with Minecraft 26.2, Loader 0.20.1, API 0.161.0+26.2 and the matching installable SurvivalFlight JAR.
3. Put `build/smoke-test/survivalflight-smoketest.jar` into that temporary directory's mods folder only.
4. Add JVM argument `-Dsurvivalflight.smokeTest=true` and load a disposable world with commands enabled. This opt-in is required; the fixture does nothing without it. It refuses to operate on a remote server.
5. Capture stdout. Success is `[SF-SMOKE] ALL PASS`; an assertion failure includes its stage. The client stops after completion/failure.

The test uses F7 through vanilla KeyMapping and jump input through normal options, checks real client/server tool speeds and acknowledged block breaks, a protected 29-block landing, a later unprotected fall, and normal void damage/death in the Overworld, Nether and End. It reads server state on the server thread. Fixture health initialization is test setup, not a mod immunity mechanism.

Do not distribute this fixture as an installable release mod. No game binaries or test worlds are committed.

## Hurt-motion reproduction and regression

Use `-Dsurvivalflight.hurtSmokeTest=true` instead of the normal suite flag. The fixture spawns a real NoAI Blaze owner and its vanilla SmallFireball, measures actual projectile/fire damage and hover displacement, and logs raw versus applied motion packets. It also tests synthetic motion-handler controls for exact X/Z preservation, existing ascent, upward knockback, Shift descent, no hurt window, other entities and F7 OFF. These controls are test setup, not production damage or movement cheats.

For the published 1.0.0 baseline only, also set `-Dsurvivalflight.expectHurtDrop=true`: success is `BASELINE REPRODUCED`, and the player may land/end flying after dropping. For the fixed version, omit that flag: success is `[SF-HURT] ALL PASS`. Use separate disposable game directories and keep the old/new installable JARs separate. Enable only one suite flag per run. All fixture code/mixins are excluded from the normal JAR.

## Sprint-descent correction reproduction

Use `-Dsurvivalflight.sprintSmokeTest=true` alone for the input/correction suite. It uses native forward/sprint/Shift/jump key mappings, counts real incoming position corrections without modifying them, and tests sprint-forward, sprint+Shift-forward, regular Shift-forward, release/press transitions, ascent and a Shift-forward protected landing after F7 OFF. Release-Shift hover measurements allow vanilla inertia to settle, while corrections are counted throughout the transition.

For the published 1.1.1 baseline only, also set `-Dsurvivalflight.expectSprintRebound=true`; success is `BASELINE REPRODUCED` with more than one actual correction. For the fixed build, omit that flag and require `[SF-SPRINT] ALL PASS`. The fixture's position-packet trace is read-only and is excluded from the installable JAR.
