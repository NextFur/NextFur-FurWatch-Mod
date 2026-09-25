package net.nextfur.fwc.economy.data;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.OptionalLong;

public class EconomyFormatHelper {
    public static final String CURRENCY_SYMBOL = "$M";
    public static final String CURRENCY_CENT = "c";
    public static final String CURRENCY_NAME = "Oceanic Mark";

    private static final DecimalFormat NUMBER_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        NUMBER_FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    /**
     * Formats cents to standard monetary string: "$M 1,250.50"
     */
    public static String formatStandard(long cents) {
        boolean negative = cents < 0;
        long absCents = Math.abs(cents);
        double marks = absCents / 100.0;
        String formatted = NUMBER_FORMAT.format(marks);
        return (negative ? "-" : "") + CURRENCY_SYMBOL + " " + formatted;
    }

    /**
     * Formats cents into traditional denomination string: "1250$M 50c" or "50c$M"
     */
    public static String formatDenomination(long cents) {
        boolean negative = cents < 0;
        long absCents = Math.abs(cents);
        long marks = absCents / 100;
        long remainderCents = absCents % 100;

        StringBuilder sb = new StringBuilder();
        if (negative) sb.append("-");

        if (marks > 0 && remainderCents > 0) {
            sb.append(marks).append(CURRENCY_SYMBOL).append(" ").append(remainderCents).append(CURRENCY_CENT);
        } else if (marks > 0) {
            sb.append(marks).append(CURRENCY_SYMBOL);
        } else {
            sb.append(remainderCents).append(CURRENCY_CENT).append(CURRENCY_SYMBOL);
        }
        return sb.toString();
    }

    /**
     * Formats both standard and denomination: "$M 1,250.50 (1250$M 50c)"
     */
    public static String formatFull(long cents) {
        return formatStandard(cents) + " (" + formatDenomination(cents) + ")";
    }

    /**
     * Parses user input string (e.g. "150", "150.50", "150,50", "50c") into cents.
     */
    public static OptionalLong parseToCents(String input) {
        if (input == null || input.trim().isEmpty()) {
            return OptionalLong.empty();
        }

        String cleaned = input.trim()
                .replace("$M", "")
                .replace("$m", "")
                .replace("M", "")
                .replace("m", "")
                .replace(" ", "");

        boolean isCentsOnly = cleaned.endsWith("c") || cleaned.endsWith("C");
        if (isCentsOnly) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
            try {
                long c = Long.parseLong(cleaned);
                return OptionalLong.of(c);
            } catch (NumberFormatException e) {
                return OptionalLong.empty();
            }
        }

        cleaned = cleaned.replace(",", ".");
        try {
            double value = Double.parseDouble(cleaned);
            if (Double.isNaN(value) || Double.isInfinite(value) || value < 0) {
                return OptionalLong.empty();
            }
            long cents = Math.round(value * 100.0);
            return OptionalLong.of(cents);
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }
}
