package com.turog.mockapi;

public record Configuration(
        String identifier,
        String endpoint,
        String publicKey,
        String secretKey,
        String encryptionKey
) {}