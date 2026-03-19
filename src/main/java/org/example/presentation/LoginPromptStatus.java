package org.example.presentation;

/**
 * Represents the terminal outcome status of a login prompt interaction.
 * SUCCESS: user authenticated successfully.
 * FAILED: user failed to authenticate (attempt remaining).
 * LOCKED: too many failed attempts; user must wait before retrying.
 * CANCELLED: user explicitly cancelled the login.
 */
public enum LoginPromptStatus {
    SUCCESS,
    FAILED,
    LOCKED,
    CANCELLED
}
