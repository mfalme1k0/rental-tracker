package com.rentaltracker;

/**
 * Composition root: the ONE place that builds the object graph (DatabaseManager -> repositories -> services ->
 * menus) with plain constructor injection. PLACEHOLDER until the layers exist; keep it thin so it stays testable.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        System.out.println("Rental tracker: foundation only, layers not wired yet.");
    }
}
