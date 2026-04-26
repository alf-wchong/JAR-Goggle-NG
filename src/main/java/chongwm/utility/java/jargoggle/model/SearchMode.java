package chongwm.utility.java.jargoggle.model;

public enum SearchMode {
    SUBSTRING("Substring"),
    EXACT("Exact"),
    GLOB("Glob");

    private final String displayName;

    SearchMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
