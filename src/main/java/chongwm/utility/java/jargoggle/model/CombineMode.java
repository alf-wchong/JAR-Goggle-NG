package chongwm.utility.java.jargoggle.model;

public enum CombineMode {
    REPLACE("Replace"),
    INTERSECT("Intersect"),
    UNION("Union"),
    SUBTRACT("Subtract");

    private final String displayName;

    CombineMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
