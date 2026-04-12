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

    /**
     * Returns the text label shown in the UI.
     *
     * @return human-readable name for this appointment type
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the display name of this appointment type.
     *
     * @return formatted name used in combo boxes and tables
     */
    @Override
    public String toString() {
        return displayName;
    }
}
