package com.ansh.atlaspractice.settings;

public enum MapSelectionPreference {

    NONE("None"),
    DUEL("Duel"),
    QUEUE("Queue"),
    ALL("All");

    private final String displayName;

    MapSelectionPreference(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MapSelectionPreference next() {
        MapSelectionPreference[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public boolean allowsDuel() {
        return this == DUEL || this == ALL;
    }
    public boolean allowsQueue() {
        return this == QUEUE || this == ALL;
    }
}