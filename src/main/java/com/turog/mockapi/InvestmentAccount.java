package com.turog.mockapi;

public record InvestmentAccount(
        String id,
        String name,
        String initials,
        String accountNumber,
        String assetType,
        String riskProfile,
        Double totalValue
) {}