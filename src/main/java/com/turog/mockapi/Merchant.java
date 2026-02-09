package com.turog.mockapi;

import java.util.List;

public record Merchant(
        String id,
        String name,
        String category,
        String price,
        String logo,
        String description,
        List<String> features,
        List<String> pricing,
        Configuration configuration
) {}