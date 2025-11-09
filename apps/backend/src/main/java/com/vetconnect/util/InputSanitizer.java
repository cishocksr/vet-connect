package com.vetconnect.util;

import org.springframework.stereotype.Component;
import org.owasp.encoder.Encode;

/**
 * Sanitizes user input to prevent XSS attacks
 * Uses OWASP Java Encoder library
 *
 * SECURITY NOTE:
 * This provides defense-in-depth against XSS attacks.
 * Always use this for any user-provided text that will be:
 * - Displayed in web pages
 * - Stored in database
 * - Returned in API responses
 */
@Component
public class InputSanitizer {

    /**
     * Sanitize HTML input
     * Encodes HTML special characters to prevent XSS
     *
     * Example: <script>alert('xss')</script>
     *       -> &lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;
     */
    public String sanitizeHtml(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forHtml(input);
    }

    /**
     * Sanitize for use in JavaScript
     * Use when embedding user input in JavaScript code
     */
    public String sanitizeJavaScript(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forJavaScript(input);
    }
}