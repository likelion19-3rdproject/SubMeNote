package com.backend.settlement.util;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

// 주/월 계산을 서비스가 공통으로 쓰게 처리
public class SettlementPeriod {
    private SettlementPeriod() {}

    // 지난주(월~일)
    public static Range lastWeekMonToSun(LocalDate today) {
        LocalDate startOfThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate start = startOfThisWeek.minusWeeks(1);
        LocalDate end = start.plusDays(6);
        return new Range(start.atStartOfDay(), end.atTime(23, 59, 59));
    }

    // 전월(1일~말일)
    public static Range lastMonth(LocalDate today) {
        LocalDate firstDayOfThisMonth = today.withDayOfMonth(1);
        LocalDate firstDayOfLastMonth = firstDayOfThisMonth.minusMonths(1);
        LocalDate lastDayOfLastMonth = firstDayOfThisMonth.minusDays(1);

        return new Range(firstDayOfLastMonth.atStartOfDay(), lastDayOfLastMonth.atTime(23, 59, 59));
    }

    public record Range(LocalDateTime start, LocalDateTime end) {}
}
