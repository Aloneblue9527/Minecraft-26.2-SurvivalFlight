package dev.survivalflight;

/** Regression checks for ownership/restoration of vanilla flight flags. */
public final class FlightSessionTest {
    private static int checks;
    private static void require(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static final class Flags implements FlightSession.FlightFlags {
        boolean allowed;
        boolean airborne;
        public boolean mayfly() { return allowed; }
        public boolean flying() { return airborne; }
        public void mayfly(boolean value) { allowed = value; }
        public void flying(boolean value) { airborne = value; }
    }
    public static void main(String[] args) {
        FlightSession session = new FlightSession();
        Flags survival = new Flags();
        require(!session.enabled(), "Default must be OFF");
        session.enable(survival);
        require(survival.allowed && !survival.airborne, "Enable permission, require vanilla double jump");
        survival.airborne = true;
        session.disable();
        require(!session.enabled() && !survival.allowed && !survival.airborne, "Restore survival flags");
        session.disable();
        require(!survival.allowed, "Repeated cleanup must be harmless");

        Flags nativeFlight = new Flags();
        nativeFlight.allowed = nativeFlight.airborne = true;
        session.enable(nativeFlight);
        session.disable();
        require(nativeFlight.allowed && nativeFlight.airborne, "Preserve pre-existing native flight");

        session.enable(survival);
        // A server packet supplies new authoritative flags before forget().
        survival.allowed = survival.airborne = true;
        session.forget();
        session.disable();
        require(survival.allowed && survival.airborne && !session.enabled(), "Do not restore stale flags over a server grant");
        session.enable(survival);
        survival.allowed = survival.airborne = false;
        session.forget();
        session.maintainPermission();
        require(!survival.allowed && !survival.airborne, "Respect server revocation; remain OFF");

        Flags nextPlayer = new Flags();
        session.enable(nextPlayer);
        nextPlayer.airborne = true;
        session.disable();
        require(!nextPlayer.allowed && !nextPlayer.airborne, "Fresh player gets a fresh baseline");
        require(!survival.allowed, "Old player must not be changed by a later session");
        session.enable(nextPlayer);
        try {
            session.enable(survival);
            throw new AssertionError("Duplicate enable must be rejected");
        } catch (IllegalStateException expected) {
            require(!survival.allowed, "Duplicate enable cannot change another player");
        }
        session.disable();
        System.out.println("FlightSession: " + checks + " regression checks passed");
    }
}
