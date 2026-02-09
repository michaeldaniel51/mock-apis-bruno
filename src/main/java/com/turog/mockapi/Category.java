package com.turog.mockapi;

import java.util.List;

public record Category(
        String title,
        List<Merchant> merchants
) {}
