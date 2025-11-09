package com.vetconnect.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for trusted proxy servers
 *
 * PURPOSE: Prevent IP spoofing attacks
 *
 * SECURITY:
 * - Only accept X-Forwarded-For headers from known proxies/load balancers
 * - In production, this should be your actual proxy/load balancer IPs
 * - Without this, attackers can bypass rate limiting by faking their IP
 *
 * EXAMPLE ATTACK WITHOUT THIS:
 * Attacker sends: X-Forwarded-For: 1.2.3.4
 * Rate limiting uses 1.2.3.4 instead of attacker's real IP
 * Attacker can bypass rate limits by changing header value
 *
 * EXAMPLE DEPLOYMENT SCENARIOS:
 * - Behind AWS ALB: Set to ALB's private IP range
 * - Behind nginx: Set to nginx server IP
 * - Behind Cloudflare: Set to Cloudflare IP ranges
 * - Local development: Empty (trust direct connections only)
 */
@Configuration
@Getter
public class TrustedProxyConfig {

    @Value("${app.security.trusted-proxies:}")
    private String trustedProxiesString;

    private Set<String> trustedProxies;

    /**
     * Initialize trusted proxies from configuration
     */
    public TrustedProxyConfig(@Value("${app.security.trusted-proxies:}") String trustedProxiesString) {
        this.trustedProxiesString = trustedProxiesString;
        this.trustedProxies = parseTrustedProxies(trustedProxiesString);
    }

    /**
     * Parse comma-separated proxy IPs
     */
    private Set<String> parseTrustedProxies(String proxies) {
        if (proxies == null || proxies.trim().isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(proxies.split(",")));
    }

    /**
     * Check if an IP address is a trusted proxy
     *
     * @param ipAddress IP address to check
     * @return true if trusted, false otherwise
     */
    public boolean isTrustedProxy(String ipAddress) {
        if (ipAddress == null || trustedProxies.isEmpty()) {
            return false;
        }
        return trustedProxies.contains(ipAddress.trim());
    }

    /**
     * Check if we have any trusted proxies configured
     *
     * @return true if trusted proxies are configured
     */
    public boolean hasTrustedProxies() {
        return !trustedProxies.isEmpty();
    }
}
