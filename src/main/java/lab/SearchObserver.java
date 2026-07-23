package lab;

/** Receives exact search progress and inspectable expansion events on the runner thread. */
public interface SearchObserver {
    void onProgress(SearchExperimentRunner.Progress progress);

    void onExpansion(SearchExpansion expansion);

    /** Allows bounded UI traces while still permitting complete observers for export and tests. */
    default boolean observesExpansion(int index) {
        return true;
    }
}
