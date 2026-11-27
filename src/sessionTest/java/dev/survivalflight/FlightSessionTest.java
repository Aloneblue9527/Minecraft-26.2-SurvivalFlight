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
        FlightSafety safety = new FlightSafety();
        require(!safety.protects(false, false, true, 64, -64), "No protection by default");
        require(!safety.protects(true, false, true, 64, -64), "Enabling alone does not arm a normal jump");
        require(safety.protects(true, true, true, 64, -64), "First actual flight packet is protected");
        safety.observe(true, true, false);
        require(safety.protects(false, false, true, 64, -64), "One pending landing after F7 OFF");
        for (int minY : new int[] {-64, 0, 0}) {
            require(!safety.protects(true, true, true, minY - 1, minY), "No below-minimum ground report in any vanilla dimension");
            require(safety.protects(true, true, true, minY, minY), "Build minimum remains eligible");
        }
        require(!safety.protects(true, true, false, 64, -64), "Invalid context/fluid/vehicle must not be protected");
        safety.observe(false, false, true);
        require(!safety.protects(false, false, true, 64, -64), "Landing ends grace; no later jump immunity");
        safety.observe(true, true, false);
        safety.reset();
        require(!safety.protects(false, false, true, 64, -64), "Reset clears pending landing");
        require(FlightSafety.hurtVertical(0, -2.5, true) == 0, "Hurt sync must not replace hovering with server gravity");
        require(FlightSafety.hurtVertical(0.15, -2.5, true) == 0.15, "Keep existing intentional ascent");
        require(FlightSafety.hurtVertical(-0.1, -2.5, true) == 0, "No existing residual descent while stabilizing");
        require(FlightSafety.hurtVertical(0, 0.4, true) == 0.4, "Keep upward knockback");
        require(FlightSafety.hurtVertical(0, 0, true) == 0, "Keep zero vertical sync");
        require(FlightSafety.hurtVertical(0, -2.5, false) == -2.5, "Outside the hurt-flight scope retain server motion");
        require(FlightSafety.hurtVertical(0.15, -0.4, false) == -0.4, "Manual descent is not stabilized");
        require(FlightSafety.needsGroundClear(true, true, true, true), "Protected airborne Shift positions need an airborne preface");
        require(!FlightSafety.needsGroundClear(false, true, true, true), "No extra packet outside protection");
        require(!FlightSafety.needsGroundClear(true, false, true, true), "Status/rotation packets cannot recurse");
        require(!FlightSafety.needsGroundClear(true, true, false, true), "Physical grounded sneaking stays vanilla");
        require(!FlightSafety.needsGroundClear(true, true, true, false), "No extra packet without Shift");
        require(!FlightSafety.needsGroundClear(false, false, false, false), "All-disabled scope stays inactive");
        System.out.println("FlightSession: " + checks + " regression checks passed");
    }
}
