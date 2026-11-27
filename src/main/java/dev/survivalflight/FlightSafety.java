package dev.survivalflight;

/** One landing of experimental protection after a real Mod flight; no health changes. */
final class FlightSafety {
    private boolean landing;

    void observe(boolean enabled, boolean flying, boolean onGround) {
        if (enabled && flying) landing = true;
        else if (onGround) landing = false;
    }

    boolean protects(boolean enabled, boolean flying, boolean eligible, double y, int minY) {
        // Use the current world's build minimum. Never extend protection into the void.
        return eligible && y >= minY && (landing || (enabled && flying));
    }

    void reset() { landing = false; }
}
