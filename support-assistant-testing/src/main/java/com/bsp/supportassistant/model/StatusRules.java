package com.bsp.supportassistant.model;

import java.util.List;

// The order of a ticket's life: OPEN, IN_PROGRESS, RESOLVED, CLOSED.
// A ticket moves one step forward at a time. This class uses no Spring and no database.
public final class StatusRules {

    private static final List<String> ORDER = List.of("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED");

    private StatusRules() {
    }

    public static boolean isAllowed(String from, String to) {
        int position = ORDER.indexOf(from);
        return position >= 0 && position + 1 < ORDER.size() && ORDER.get(position + 1).equals(to);
    }
}
