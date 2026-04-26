package chongwm.utility.java.jargoggle.model;

public enum SearchTargetScope {
    ALL_ROOTS("All roots"),
    CURRENT_RESULT_ONLY("Current result set only"),
    CURRENT_RESULT_AND_ROOTS("Current result set + roots");

    private final String displayName;

    SearchTargetScope(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
