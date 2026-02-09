package com.turog.mockapi;

public record SavingsAccount(
        String id,
        String name,
        String initials,
        String externalReference,
        String accountNumber,
        String status,
        Double dailyLimit
) {}