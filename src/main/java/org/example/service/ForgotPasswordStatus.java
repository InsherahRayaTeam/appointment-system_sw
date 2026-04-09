package org.example.service;

/**
 * Lists the possible values for forgot password operations.
 */
public enum ForgotPasswordStatus {
    RESET_REQUESTED,
    PASSWORD_RESET_SUCCESS,
    BLANK_IDENTIFIER,
    UNKNOWN_USER,
    INVALID_RESET_CODE,
    BLANK_NEW_PASSWORD,
    PASSWORD_MISMATCH,
    WEAK_PASSWORD,
    UPDATE_FAILED
}

