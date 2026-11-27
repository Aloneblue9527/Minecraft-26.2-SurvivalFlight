package dev.survivalflight;

/** Owns only the two flight flags; speed, health, hunger and interactions are untouched. */
final class FlightSession {
    interface FlightFlags {
        boolean mayfly();
        boolean flying();
        void mayfly(boolean value);
        void flying(boolean value);
    }

    private FlightFlags flags;
    private boolean previousMayfly;
    private boolean previousFlying;

    boolean enabled() { return flags != null; }

    void enable(FlightFlags current) {
        if (enabled()) throw new IllegalStateException("Flight already enabled");
        previousMayfly = current.mayfly();
        previousFlying = current.flying();
        flags = current;
        flags.mayfly(true);
    }

    void disable() {
        if (flags != null) {
            flags.mayfly(previousMayfly);
            flags.flying(previousMayfly && previousFlying);
            forget();
        }
    }

    /** A server abilities packet or native flight mode supersedes our saved flags. */
    void forget() { flags = null; }

    void maintainPermission() {
        if (flags != null) flags.mayfly(true);
    }
}
