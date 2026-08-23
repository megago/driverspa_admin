package com.driverspa.util;

import com.driverspa.BA;
import com.driverspa.R;

import java.util.HashMap;
import java.util.Map;

/**
 * The analytics endpoint returns statistic titles / item names as plain text in a
 * single language (currently English), so they don't follow the app's selected
 * language like the rest of the UI. This maps the known backend labels — matched
 * case/spacing-insensitively, in both English and Russian — to localized
 * resources. Unknown labels (dynamic names, amounts) are returned unchanged.
 *
 * <p>If a label still appears untranslated, add its exact backend text as another
 * variant in {@link #put}.
 */
public final class StatisticLabels {

    private static final Map<String, Integer> MAP = new HashMap<>();

    private StatisticLabels() {
    }

    private static void put(int resId, String... variants) {
        for (String v : variants) MAP.put(norm(v), resId);
    }

    static {
        put(R.string.stat_total_statistics, "total statistics", "общая статистика");
        put(R.string.general_metrics, "general metrics", "general statistics", "общие показатели");
        put(R.string.stat_avg_wash_time, "average wash time", "avg wash time", "среднее время мойки", "средняя длительность мойки");
        put(R.string.stat_number_of_washes, "number of washes", "wash count", "washes count", "количество моек", "количество мойки");
        put(R.string.total_amount, "total amount", "общая сумма");
        put(R.string.by_employees, "by staff", "by employees", "statistics by staff", "by staff members", "по сотрудникам");
        put(R.string.by_boxes, "by boxes", "statistics by boxes", "по боксам");
        put(R.string.by_services, "by services", "statistics by services", "по услугам");
        put(R.string.discounts, "discounts", "скидки");
        put(R.string.income_word, "income", "incomes", "доход", "доходы");
        put(R.string.total_expenses, "expenses", "expense", "расход", "расходы");
        put(R.string.stat_net_profit, "net profit", "чистая прибыль");
        put(R.string.cash, "cash", "наличные");
        put(R.string.card, "card", "карта");
        put(R.string.transfer, "transfer", "перечисление");
        put(R.string.bonuses, "bonuses", "бонусы");
    }

    private static String norm(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase().replaceAll("[\\s:]+", " ").trim();
    }

    /** Returns the localized label if known, otherwise the original text unchanged. */
    public static String localize(String backendLabel) {
        if (backendLabel == null) return null;
        Integer resId = MAP.get(norm(backendLabel));
        return resId != null ? BA.str(resId) : backendLabel;
    }
}
