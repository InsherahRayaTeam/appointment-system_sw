package org.example;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainAdminMenuBookingFlowTest {

    @Test
    void main_LoginBookThenLogout_ShowsExpectedMessages() {
        String input = String.join("\n",
                "admin",
                "admin",
                "8",
                "10:00",
                "9",
                "exit"
        ) + "\n";

        String printed = runMainAndCaptureOutput(input);

        assertTrue(printed.contains("Login successful."));
        assertTrue(printed.contains("Success: Appointment booked for 10:00."));
        assertTrue(printed.contains("You have been logged out successfully."));
    }

    @Test
    void main_InvalidMenuChoiceThenValidChoice_HandlesGracefully() {
        String input = String.join("\n",
                "admin",
                "admin",
                "0",
                "7",
                "9",
                "exit"
        ) + "\n";

        String printed = runMainAndCaptureOutput(input);

        assertTrue(printed.contains("Invalid choice. Please enter a number between 7 and 9."));
        assertTrue(printed.contains("Available Appointment Slots"));
    }

    private String runMainAndCaptureOutput(String input) {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            Main.main(new String[0]);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        return output.toString(StandardCharsets.UTF_8);
    }
}
