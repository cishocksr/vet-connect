package com.vetconnect.util;

import org.springframework.stereotype.Component;
import org.owasp.encoder.Encode;

/**
 * Sanitizes user input to prevent XSS attacks
 * Uses OWASP Java Encoder library
 */
@Component
public class InputSanitizer {

    /**
     * Sanitize HTML input
     * Removes potentially dangerous HTML tags and scripts
     */
    public String sanitizeHtml(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forHtml(input);
    }

    /**
     * Sanitize for use in JavaScript
     */
    public String sanitizeJavaScript(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forJavaScript(input);
    }

    /**
     * Sanitize SQL input (additional layer beyond JPA)
     */
    public String sanitizeSql(String input) {
        if (input == null) {
            return null;
        }
        // Remove SQL injection characters
        return input.replaceAll("[';\"\\-\\-]", "");
    }
}