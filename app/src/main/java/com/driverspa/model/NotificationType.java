package com.driverspa.model;

import com.google.gson.annotations.SerializedName;

public enum NotificationType {

    @SerializedName("HOME")
    HOME("HOME"),

    @SerializedName("PROFILE")
    PROFILE("PROFILE"),

    @SerializedName("NOTHING")
    NOTHING("NOTHING"),

    Unknown("unknown");

    public static NotificationType toEnum (String enumString) {
        try {
            return valueOf(enumString);
        } catch (Exception ex) {
            // For error cases
            return Unknown;
        }
    }

    private final String id;
    NotificationType(String id) { this.id = id; }
    public String getValue() { return id; }

    public String toString() {
        return getValue();
    }

}
