package lab;

import java.util.Objects;

/** Runtime context required to interpret environment-dependent experiment metrics. */
public record RuntimeEnvironment(String javaVersion, String osName, String osVersion,
                                 String osArchitecture) {
    public RuntimeEnvironment {
        Objects.requireNonNull(javaVersion, "javaVersion");
        Objects.requireNonNull(osName, "osName");
        Objects.requireNonNull(osVersion, "osVersion");
        Objects.requireNonNull(osArchitecture, "osArchitecture");
    }

    public static RuntimeEnvironment current() {
        return new RuntimeEnvironment(
                System.getProperty("java.version", "unknown"),
                System.getProperty("os.name", "unknown"),
                System.getProperty("os.version", "unknown"),
                System.getProperty("os.arch", "unknown"));
    }
}
