package org.example.util;

/**
 * Small console output wrapper used by the application.
 *
 * Reasoning: Sonar flags direct uses of System.out. This wrapper centralizes
 * console output so we can justify and document console usage for this
 * command-line/demo application. The wrapper intentionally delegates to
 * System.out to preserve existing behavior and to avoid changing tests that
 * capture standard output.
 */
@SuppressWarnings("squid:S106")
public final class Console {

    private Console() {
        // utility class
    }

    /**
     * Prints a line to the console. Preserves exact text formatting used by
     * the original application (no added prefixes) to avoid changing behavior
     * relied on by tests.
     *
     * @param message text to print (may be null)
     */
    public static void println(String message) {
        System.out.println(message);
    }

}

