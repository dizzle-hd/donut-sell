package de.donutsell;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/** Parses prices like {@code 1500}, {@code 2.5k}, {@code 1,2m}, {@code 3b}. */
public final class Prices {
    private Prices() {
    }

    /** @return the price as a whole number, or -1 if the text is not a valid positive price */
    public static long parse(String text) {
        if (text == null) return -1;
        String s = text.trim().toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "").replace("$", "");
        if (s.isEmpty()) return -1;
        long multiplier = 1;
        char last = s.charAt(s.length() - 1);
        switch (last) {
            case 'k' -> multiplier = 1_000L;
            case 'm' -> multiplier = 1_000_000L;
            case 'b' -> multiplier = 1_000_000_000L;
            case 't' -> multiplier = 1_000_000_000_000L;
            default -> { }
        }
        if (multiplier != 1) s = s.substring(0, s.length() - 1);
        // "1,5" is a decimal comma (German), "1,500" / "1.500.000" are thousands separators
        if (s.matches("\\d{1,3}([.,]\\d{3})+")) {
            s = s.replace(".", "").replace(",", "");
        } else {
            s = s.replace(',', '.');
        }
        try {
            BigDecimal value = new BigDecimal(s).multiply(BigDecimal.valueOf(multiplier));
            long result = value.setScale(0, RoundingMode.HALF_UP).longValueExact();
            return result > 0 ? result : -1;
        } catch (NumberFormatException | ArithmeticException e) {
            return -1;
        }
    }

    /** Short display form for chat feedback, e.g. 2500000 → "2.5M". */
    public static String pretty(long value) {
        if (value >= 1_000_000_000L) return trim(value / 1_000_000_000d) + "B";
        if (value >= 1_000_000L) return trim(value / 1_000_000d) + "M";
        if (value >= 1_000L) return trim(value / 1_000d) + "K";
        return Long.toString(value);
    }

    private static String trim(double d) {
        String s = String.format(Locale.ROOT, "%.2f", d);
        return s.replaceAll("\\.?0+$", "");
    }
}
