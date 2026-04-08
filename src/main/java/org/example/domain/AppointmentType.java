package org.example.domain;

/**
 * Lists the possible values for appointment type.
 */
public enum AppointmentType {
    NORMAL("Normal"),
    URGENT("Urgent"),
    FOLLOW_UP("Follow-up"),
    ASSESSMENT("Assessment"),
    VIRTUAL("Virtual"),
    IN_PERSON("In-person"),
    INDIVIDUAL("Individual"),
    GROUP("Group");

    private final String displayName;

    AppointmentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
