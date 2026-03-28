package org.example.presentation;

import java.util.Objects;
import java.util.Scanner;

/**
 * Handles console input concerns such as prompting, trimming, and basic validation.
 *
 * @author appointment-system
 * @version 1.0
 */
public class ConsoleInputHandler {

    private final Scanner scanner;

    /**
     * Creates an input helper bound to a scanner.
     *
     * @param scanner scanner used for console input
     */
    public ConsoleInputHandler(Scanner scanner) {
        this.scanner = Objects.requireNonNull(scanner, "scanner cannot be null");
    }

    /**
     * Reads a single line after printing a prompt.
     *
     * @param prompt prompt text
     * @return raw line entered by the user
     */
    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /**
     * Reads a non-blank line, repeating until valid input is provided.
     *
     * @param prompt prompt text
     * @return trimmed non-blank value
     */
    public String readRequiredLine(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
            System.out.println("Input is required. Please try again.");
        }
    }

    /**
     * Reads and validates a numeric menu choice in the allowed range.
     *
     * @param prompt prompt text
     * @param min minimum allowed choice
     * @param max maximum allowed choice
     * @return validated menu choice
     */
    public int readMenuChoice(String prompt, int min, int max) {
        while (true) {
            String rawChoice = readLine(prompt).trim();
            try {
                int choice = Integer.parseInt(rawChoice);
                if (choice >= min && choice <= max) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {
                // Continue loop and print friendly guidance.
            }
            System.out.println("Invalid choice. Please enter a number between " + min + " and " + max + ".");
        }
    }
}
