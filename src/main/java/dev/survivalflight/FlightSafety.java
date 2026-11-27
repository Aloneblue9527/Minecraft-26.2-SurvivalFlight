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

    /** Damage can synchronize the server's falling velocity over local flight. */
    static double hurtVertical(double currentY, double incomingY, boolean stabilize) {
        return stabilize && incomingY < 0 ? Math.max(0, currentY) : incomingY;
    }

    static boolean needsGroundClear(boolean protectedReport, boolean hasPosition,
                                    boolean physicallyAirborne, boolean shift) {
        return protectedReport && hasPosition && physicallyAirborne && shift;
    }
}
