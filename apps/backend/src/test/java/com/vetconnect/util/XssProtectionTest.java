package com.vetconnect.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XssProtectionTest {

    private XssProtection xssProtection;

    @BeforeEach
    void setUp() {
        xssProtection = new XssProtection();
    }

    // ========== sanitize() tests ==========

    @Test
    void sanitize_WithNormalText_ShouldReturnUnchanged() {
        String input = "Hello World";
        String result = xssProtection.sanitize(input);
        assertEquals("Hello World", result);
    }

    @Test
    void sanitize_WithNull_ShouldReturnNull() {
        String result = xssProtection.sanitize(null);
        assertNull(result);
    }

    @Test
    void sanitize_WithEmptyString_ShouldReturnEmpty() {
        String result = xssProtection.sanitize("");
        assertEquals("", result);
    }

    @Test
    void sanitize_WithScriptTag_ShouldRemoveIt() {
        String input = "<script>alert('xss')</script>";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        assertFalse(result.contains("<script>"));
        assertFalse(result.toLowerCase().contains("script"));
    }

    @Test
    void sanitize_WithScriptTagUppercase_ShouldRemoveIt() {
        String input = "<SCRIPT>alert('xss')</SCRIPT>";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        assertFalse(result.toLowerCase().contains("script"));
    }

    @Test
    void sanitize_WithOnErrorHandler_ShouldRemoveIt() {
        String input = "<img src='x' onerror='alert(1)'>";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        assertFalse(result.toLowerCase().contains("onerror"));
    }

    @Test
    void sanitize_WithOnClickHandler_ShouldRemoveIt() {
        String input = "<div onclick='alert(1)'>Click me</div>";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        assertFalse(result.toLowerCase().contains("onclick"));
    }

    @Test
    void sanitize_WithOnLoadHandler_ShouldRemoveIt() {
        String input = "<body onload='alert(1)'>";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        assertFalse(result.toLowerCase().contains("onload"));
    }

    @Test
    void sanitize_WithOnMouseOverHandler_ShouldRemoveIt() {
        String input = "<a onmouseover='alert(1)'>Hover</a>";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        assertFalse(result.toLowerCase().contains("onmouseover"));
    }

    @Test
    void sanitize_WithJavaScriptProtocol_ShouldRemoveIt() {
        String input = "<a href='javascript:alert(1)'>Click</a>";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        assertFalse(result.toLowerCase().contains("javascript:"));
    }

    @Test
    void sanitize_WithVbScriptProtocol_ShouldRemoveIt() {
        String input = "<a href='vbscript:msgbox(1)'>Click</a>";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        assertFalse(result.toLowerCase().contains("vbscript:"));
    }

    @Test
    void sanitize_WithEvalFunction_ShouldRemoveIt() {
        String input = "eval(alert(1))";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        assertFalse(result.toLowerCase().contains("eval("));
    }

    @Test
    void sanitize_WithExpressionFunction_ShouldRemoveIt() {
        String input = "expression(alert(1))";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        assertFalse(result.toLowerCase().contains("expression("));
    }

    @Test
    void sanitize_WithIframeTag_ShouldRemoveOpeningTag() {
        String input = "<iframe src='evil.com'></iframe>";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        // Pattern removes <iframe...> but </iframe> remains and gets encoded
        assertFalse(result.contains("<iframe"));
        // The remaining content will have HTML encoding
        assertTrue(result.contains("&lt;") || result.contains("&gt;"));
    }

    @Test
    void sanitize_WithObjectTag_ShouldRemoveOpeningTag() {
        String input = "<object data='evil.swf'></object>";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        // Pattern removes <object...> but </object> remains and gets encoded
        assertFalse(result.contains("<object"));
        // The remaining content will have HTML encoding
        assertTrue(result.contains("&lt;") || result.contains("&gt;"));
    }

    @Test
    void sanitize_WithEmbedTag_ShouldRemoveIt() {
        String input = "<embed src='evil.swf'>";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        assertFalse(result.toLowerCase().contains("embed"));
    }

    @Test
    void sanitize_WithMultipleXssVectors_ShouldRemoveAll() {
        String input = "<script>bad</script><img src=x onerror=alert(1)><iframe src=evil>";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        assertFalse(result.toLowerCase().contains("script"));
        assertFalse(result.toLowerCase().contains("onerror"));
        assertFalse(result.toLowerCase().contains("iframe"));
    }

    @Test
    void sanitize_ShouldEncodeHtmlSpecialChars() {
        String input = "Test < > \" ' / characters";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        // Special characters should be encoded
        assertTrue(result.contains("&lt;"));
        assertTrue(result.contains("&gt;"));
        assertTrue(result.contains("&quot;"));
        assertTrue(result.contains("&#x27;"));
        assertTrue(result.contains("&#x2F;"));
    }

    @Test
    void sanitize_WithNormalTextAndSpecialChars_ShouldOnlyEncodeSpecialChars() {
        String input = "Hello <World>";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("World"));
        assertTrue(result.contains("&lt;"));
        assertTrue(result.contains("&gt;"));
    }

    @Test
    void sanitize_WithSrcAttribute_ShouldRemoveIt() {
        String input = "<img src='javascript:alert(1)'>";
        String result = xssProtection.sanitize(input);

        assertNotNull(result);
        // Should remove dangerous src patterns
        assertFalse(result.contains("javascript:"));
    }

    // ========== sanitizeAndTruncate() tests ==========

    @Test
    void sanitizeAndTruncate_WithShortText_ShouldNotTruncate() {
        String input = "Short text";
        String result = xssProtection.sanitizeAndTruncate(input, 100);

        assertNotNull(result);
        assertTrue(result.length() <= 100);
    }

    @Test
    void sanitizeAndTruncate_WithLongText_ShouldTruncate() {
        String input = "A".repeat(200);
        String result = xssProtection.sanitizeAndTruncate(input, 100);

        assertNotNull(result);
        assertEquals(100, result.length());
    }

    @Test
    void sanitizeAndTruncate_WithXssAndLongText_ShouldSanitizeAndTruncate() {
        String input = "<script>alert(1)</script>" + "A".repeat(200);
        String result = xssProtection.sanitizeAndTruncate(input, 100);

        assertNotNull(result);
        assertFalse(result.contains("<script>"));
        assertTrue(result.length() <= 100);
    }

    @Test
    void sanitizeAndTruncate_WithNull_ShouldReturnNull() {
        String result = xssProtection.sanitizeAndTruncate(null, 100);
        assertNull(result);
    }

    @Test
    void sanitizeAndTruncate_WithExactMaxLength_ShouldNotTruncate() {
        String input = "A".repeat(50);
        String result = xssProtection.sanitizeAndTruncate(input, 50);

        assertNotNull(result);
        assertEquals(50, result.length());
    }

    @Test
    void sanitizeAndTruncate_ShouldSanitizeBeforeTruncating() {
        // XSS payload that becomes longer after sanitization (due to HTML encoding)
        String input = "<script>" + "A".repeat(90) + "</script>";
        String result = xssProtection.sanitizeAndTruncate(input, 100);

        assertNotNull(result);
        assertFalse(result.contains("<script>"));
        assertTrue(result.length() <= 100);
    }
}