package com.vetconnect.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * XSS Protection Utility
 *
 * Sanitizes user input to prevent Cross-Site Scripting (XSS) attacks
 * Removes dangerous HTML/JavaScript patterns from input strings
 *
 * USE CASES:
 * - Resource descriptions
 * - User notes on saved resources
 * - Any user-generated content displayed in HTML
 *
 * NOTE: This is a basic implementation. For comprehensive XSS protection,
 * consider using OWASP Java HTML Sanitizer library in production.
 */
@Component
public class XssProtection {

    /**
     * Patterns that indicate potential XSS attacks
     * These patterns are removed from user input
     */
    private static final Pattern[] XSS_PATTERNS = {
            // Script tags
            Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE),

            // JavaScript event handlers
            Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            // JavaScript execution
            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),

            // Event handlers
            Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onclick(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onmouseover(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            // Other dangerous patterns
            Pattern.compile("<iframe(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("<object(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("<embed(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };

    /**
     * Sanitize input string by removing XSS patterns
     *
     * @param value Input string that may contain XSS attacks
     * @return Sanitized string safe for display
     */
    public String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        String sanitized = value;

        // Apply all XSS patterns
        for (Pattern pattern : XSS_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("");
        }

        // Also encode special HTML characters
        sanitized = sanitized
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");

        return sanitized;
    }

    /**
     * Sanitize and truncate to max length
     * Useful for fields with length restrictions
     */
    public String sanitizeAndTruncate(String value, int maxLength) {
        String sanitized = sanitize(value);
        if (sanitized != null && sanitized.length() > maxLength) {
            return sanitized.substring(0, maxLength);
        }
        return sanitized;
    }
}