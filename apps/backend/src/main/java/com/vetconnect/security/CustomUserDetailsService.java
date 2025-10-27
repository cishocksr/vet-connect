package com.vetconnect.security;

import com.vetconnect.model.User;
import com.vetconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService for Spring Security
 *
 * Spring Security calls this to load user during authentication
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username (email in our case)
     *
     * Called by Spring Security during authentication
     *
     * @param email User's email address
     * @return UserDetails for authentication
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email
                ));

        return CustomUserDetails.create(user);
    }

    /**
     * Load user by ID
     *
     * Used for JWT token authentication
     *
     * @param userId User's UUID
     * @return UserDetails for authentication
     * @throws UsernameNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(java.util.UUID userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with ID: " + userId
                ));

        return CustomUserDetails.create(user);
    }
}