package com.turog.mockapi;

public record CreditAccount(
        String id, String name, String initials, String accountNumber,
        String location, String status, Double dailyLimit, String currency,
        String clientType, Double loanAmount, String submissionDate
) {}