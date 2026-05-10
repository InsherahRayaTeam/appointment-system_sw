package org.example.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Encodes and verifies passwords with SHA-256 and a per-password salt.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "SHA-256";
    private static final String PREFIX = "sha256";
    private static final int SALT_BYTES = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    /**
     * Creates a salted password hash unless the value is already encoded.
     *
     * @param password raw password or an already encoded password value
     * @return encoded password hash
     */
    public static String encode(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        if (isEncoded(password)) {
            return password;
        }

        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = hash(salt, password);

        return PREFIX + ":"
                + Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifies a raw password against a stored encoded hash.
     *
     * @param rawPassword raw password supplied by the user
     * @param storedPasswordHash stored encoded password hash
     * @return true when the password matches
     */
    public static boolean matches(String rawPassword, String storedPasswordHash) {
        if (rawPassword == null || storedPasswordHash == null || !isEncoded(storedPasswordHash)) {
            return false;
        }

        String[] parts = storedPasswordHash.split(":", -1);
        if (parts.length != 3) {
            return false;
        }

        try {
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);
            byte[] actualHash = hash(salt, rawPassword);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    /**
     * Checks whether a value uses this hasher's encoded storage format.
     *
     * @param value password value
     * @return true when the value is an encoded hash
     */
    public static boolean isEncoded(String value) {
        return value != null && value.startsWith(PREFIX + ":");
    }

    private static byte[] hash(byte[] salt, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            return digest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
