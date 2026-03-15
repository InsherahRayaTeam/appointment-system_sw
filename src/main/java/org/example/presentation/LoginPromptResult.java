package org.example.presentation;

public class LoginPromptResult {

    private final LoginPromptStatus status;
    private final String username;

    public LoginPromptResult(LoginPromptStatus status, String username) {
        this.status = status;
        this.username = username;
    }

    public LoginPromptStatus getStatus() {
        return status;
    }

    public String getUsername() {
        return username;
    }

    public boolean isSuccess() {
        return status == LoginPromptStatus.SUCCESS;
    }
}
