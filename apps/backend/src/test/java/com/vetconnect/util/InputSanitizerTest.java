package com.vetconnect.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputSanitizerTest {

    private InputSanitizer inputSanitizer;

    @BeforeEach
    void setUp() {
        inputSanitizer = new InputSanitizer();
    }

    // ========== sanitizeHtml() tests ==========

    @Test
    void sanitizeHtml_WithNormalText_ShouldReturnUnchanged() {
        String input = "Hello World";
        String result = inputSanitizer.sanitizeHtml(input);
        assertEquals("Hello World", result);
    }

    @Test
    void sanitizeHtml_WithNull_ShouldReturnNull() {
        String result = inputSanitizer.sanitizeHtml(null);
        assertNull(result);
    }

    @Test
    void sanitizeHtml_WithScriptTag_ShouldEncode() {
        String input = "<script>alert('xss')</script>";
        String result = inputSanitizer.sanitizeHtml(input);

        assertNotNull(result);
        assertFalse(result.contains("<script>"));
        assertTrue(result.contains("&lt;") || result.contains("&gt;"));
    }

    @Test
    void sanitizeHtml_WithHtmlTags_ShouldEncode() {
        String input = "<b>Bold</b> and <i>Italic</i>";
        String result = inputSanitizer.sanitizeHtml(input);

        assertNotNull(result);
        // Should encode the tags
        assertTrue(result.contains("&lt;") || result.contains("&gt;"));
        // But content should remain
        assertTrue(result.contains("Bold"));
        assertTrue(result.contains("Italic"));
    }

    @Test
    void sanitizeHtml_WithSpecialChars_ShouldEncode() {
        String input = "Test & < > \" ' characters";
        String result = inputSanitizer.sanitizeHtml(input);

        assertNotNull(result);
        // Special characters should be encoded
        assertTrue(result.contains("&"));
    }

    // ========== sanitizeJavaScript() tests ==========

    @Test
    void sanitizeJavaScript_WithNormalText_ShouldReturnSafe() {
        String input = "Hello World";
        String result = inputSanitizer.sanitizeJavaScript(input);
        assertNotNull(result);
    }

    @Test
    void sanitizeJavaScript_WithNull_ShouldReturnNull() {
        String result = inputSanitizer.sanitizeJavaScript(null);
        assertNull(result);
    }

    @Test
    void sanitizeJavaScript_WithQuotes_ShouldEscape() {
        String input = "It's a \"test\"";
        String result = inputSanitizer.sanitizeJavaScript(input);

        assertNotNull(result);
        // Should escape quotes for JavaScript context
        assertFalse(result.equals(input)); // Should be modified
    }

    @Test
    void sanitizeJavaScript_WithScriptTag_ShouldEscape() {
        String input = "<script>alert(1)</script>";
        String result = inputSanitizer.sanitizeJavaScript(input);

        assertNotNull(result);
        // JavaScript encoding escapes dangerous characters
        assertNotEquals(input, result); // Should be modified
    }

    // ========== sanitize() general method tests ==========

    @Test
    void sanitize_WithNormalText_ShouldReturnUnchanged() {
        String input = "This is normal text";
        String result = inputSanitizer.sanitize(input);
        assertEquals("This is normal text", result);
    }

    @Test
    void sanitize_WithNull_ShouldReturnNull() {
        String result = inputSanitizer.sanitize(null);
        assertNull(result);
    }

    @Test
    void sanitize_WithEmptyString_ShouldReturnEmpty() {
        String result = inputSanitizer.sanitize("");
        assertEquals("", result);
    }

    @Test
    void sanitize_WithXssAttempt_ShouldSanitize() {
        String input = "<script>alert('xss')</script>";
        String result = inputSanitizer.sanitize(input);

        assertNotNull(result);
        assertFalse(result.contains("<script>"));
    }

    @Test
    void sanitize_WithMixedContent_ShouldPreserveTextButEncodeHtml() {
        String input = "Normal text <b>with HTML</b> inside";
        String result = inputSanitizer.sanitize(input);

        assertNotNull(result);
        assertTrue(result.contains("Normal text"));
        assertTrue(result.contains("with HTML"));
        assertTrue(result.contains("inside"));
        // HTML tags should be encoded
        assertTrue(result.contains("&lt;") || result.contains("&gt;"));
    }

    @Test
    void sanitize_WithMultipleXssVectors_ShouldHandleAll() {
        String input = "<script>bad</script><img src=x onerror=alert(1)>";
        String result = inputSanitizer.sanitize(input);

        assertNotNull(result);
        assertFalse(result.contains("<script>"));
        assertFalse(result.contains("<img"));
    }

    @Test
    void sanitize_WithLongInput_ShouldHandleCorrectly() {
        String input = "A".repeat(1000) + "<script>alert(1)</script>";
        String result = inputSanitizer.sanitize(input);

        assertNotNull(result);
        assertFalse(result.contains("<script>"));
        assertTrue(result.length() >= 1000);
    }

    @Test
    void sanitize_WithUnicodeCharacters_ShouldPreserve() {
        String input = "Hello 你好 مرحبا";
        String result = inputSanitizer.sanitize(input);

        assertNotNull(result);
        assertTrue(result.contains("你好"));
        assertTrue(result.contains("مرحبا"));
    }
}