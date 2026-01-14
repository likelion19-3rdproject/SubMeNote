package com.backend.settlement_item.dto;

import java.time.LocalDate;

public record SettlementItemCreateRequest(
        LocalDate startDate,
        LocalDate endDate
) {}
