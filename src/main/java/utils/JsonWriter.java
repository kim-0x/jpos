package utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public final class JsonWriter {
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMM-yyyy");

    private JsonWriter() {
    }

    /**
     * INTENT: Convert a date value to a simple month-year label used in report payloads.
     * PRECONDITION: date may be null.
     * RETURNS: a month-year string in the MMM-yyyy format (for example May-2026), or null when
     * date is null.
     * POSTCONDITION: no external state is modified while formatting the date.
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return MONTH_YEAR_FORMATTER.format(Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()));
    }

    /**
     * INTENT: Encode a raw string value as a valid JSON string literal.
     * PRECONDITION: value may be null and may contain quotes, backslashes, or control characters.
     * RETURNS: a JSON-safe quoted string, or the literal null when value is null.
     * POSTCONDITION: the input value is not modified; a serialized JSON representation is returned.
     */
    public static String toJsonString(String value) {
        if (value == null) {
            return "null";
        }

        StringBuilder escaped = new StringBuilder(value.length() + 16);
        escaped.append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        escaped.append('"');
        return escaped.toString();
    }
}
