package io.github.javamrcp.sip;

import java.util.Locale;
import java.util.Optional;

/**
 * Helpers for SIP header values used by transaction and dialog handling.
 */
public final class SipHeaderValues {
    private SipHeaderValues() {
    }

    public static Optional<String> parameter(String headerValue, String parameterName) {
        if (headerValue == null || parameterName == null || parameterName.isBlank()) {
            return Optional.empty();
        }
        String expectedName = parameterName.toLowerCase(Locale.ROOT);
        String[] segments = headerValue.split(";");
        for (int index = 1; index < segments.length; index++) {
            String segment = segments[index].trim();
            int separator = segment.indexOf('=');
            String name = separator >= 0 ? segment.substring(0, separator).trim() : segment;
            if (name.toLowerCase(Locale.ROOT).equals(expectedName)) {
                if (separator < 0) {
                    return Optional.of("");
                }
                return Optional.of(stripQuotes(segment.substring(separator + 1).trim()));
            }
        }
        return Optional.empty();
    }

    public static boolean hasParameter(String headerValue, String parameterName) {
        return parameter(headerValue, parameterName).isPresent();
    }

    public static String appendParameterIfMissing(String headerValue, String parameterName, String value) {
        if (hasParameter(headerValue, parameterName)) {
            return headerValue;
        }
        return headerValue + ";" + parameterName + "=" + value;
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
