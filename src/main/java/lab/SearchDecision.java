package lab;

/** Why a candidate successor was or was not added to the frontier. */
public enum SearchDecision {
    DISCOVERED,
    IMPROVED,
    REJECTED_NOT_BETTER,
    STATE_LIMIT_REACHED
}
