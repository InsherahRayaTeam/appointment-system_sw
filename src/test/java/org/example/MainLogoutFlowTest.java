package org.example;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainLogoutFlowTest {

    @Test
    void main_LoginThenLogout_ReturnsToLoginAndAllowsExit() {
        String demoPassword = "admin" + "123";
        String input = String.join("\n",
                "admin",
                demoPassword,
                "9",
                "exit"
        ) + "\n";

        String printed = runMainAndCaptureOutput(input);

        assertTrue(printed.contains("Login successful."));
        assertTrue(printed.contains("You have been logged out successfully."));
        assertTrue(printed.contains("Administrator Login"));
        assertTrue(printed.contains("Thank you for using Appointment System."));
    }

    @Test
    void main_ExitFromLogin_TerminatesCleanly() {
        String printed = runMainAndCaptureOutput("exit\n");

        assertTrue(printed.contains("Administrator Login"));
        assertTrue(printed.contains("Thank you for using Appointment System."));
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
