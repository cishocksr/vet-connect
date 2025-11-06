package com.vetconnect.service;

import com.vetconnect.dto.admin.*;
import com.vetconnect.dto.user.UpdateUserRequest;
import com.vetconnect.mapper.AdminMapper;
import com.vetconnect.mapper.UserMapper;
import com.vetconnect.model.User;
import com.vetconnect.model.enums.BranchOfService;
import com.vetconnect.model.enums.UserRole;
import com.vetconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Admin service for user management and system administration
 *
 * ADMIN CAPABILITIES:
 * - View all users with filtering and search
 * - View detailed user information
 * - Update user roles (promote/demote admins)
 * - Suspend/activate user accounts
 * - Update user information
 * - View system statistics
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final AdminMapper adminMapper;
    private final UserMapper userMapper;

    /**
     * Get all users with pagination and filtering
     */
    public Page<AdminUserListDTO> getAllUsers(Pageable pageable) {
        log.debug("Admin fetching all users, page: {}", pageable.getPageNumber());

        return userRepository.findAll(pageable)
                .map(adminMapper::toListDTO);
    }

    /**
     * Search users by email, name, or location
     */
    public Page<AdminUserListDTO> searchUsers(String query, Pageable pageable) {
        log.debug("Admin searching users with query: {}", query);

        return userRepository.searchUsers(query, pageable)
                .map(adminMapper::toListDTO);
    }

    /**
     * Filter users by role
     */
    public Page<AdminUserListDTO> getUsersByRole(UserRole role, Pageable pageable) {
        log.debug("Admin fetching users by role: {}", role);

        return userRepository.findByRole(role, pageable)
                .map(adminMapper::toListDTO);
    }

    /**
     * Filter users by active status
     */
    public Page<AdminUserListDTO> getUsersByActiveStatus(boolean isActive, Pageable pageable) {
        log.debug("Admin fetching users by active status: {}", isActive);

        return userRepository.findByIsActive(isActive, pageable)
                .map(adminMapper::toListDTO);
    }

    /**
     * Filter homeless users
     */
    public Page<AdminUserListDTO> getHomelessUsers(Pageable pageable) {
        log.debug("Admin fetching homeless users");

        return userRepository.findByIsHomeless(true, pageable)
                .map(adminMapper::toListDTO);
    }

    /**
     * Get detailed user information (admin view)
     */
    public AdminUserDetailDTO getUserDetails(UUID userId) {
        log.debug("Admin fetching user details: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return adminMapper.toDetailDTO(user);
    }

    /**
     * Update user role (promote/demote)
     */
    @Transactional
    public AdminUserDetailDTO updateUserRole(UUID userId, UpdateUserRoleRequest request) {
        log.info("Admin updating user role: {} to {}", userId, request.getRole());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Prevent self-demotion
        if (user.getRole() == UserRole.ADMIN && request.getRole() == UserRole.USER) {
            long adminCount = userRepository.countByRole(UserRole.ADMIN);
            if (adminCount <= 1) {
                throw new RuntimeException("Cannot demote the last admin user");
            }
        }

        user.setRole(request.getRole());
        User savedUser = userRepository.save(user);

        log.info("User role updated successfully: {}", userId);
        return adminMapper.toDetailDTO(savedUser);
    }

    /**
     * Suspend user account
     */
    @Transactional
    public AdminUserDetailDTO suspendUser(UUID userId, SuspendUserRequest request) {
        log.info("Admin suspending user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Prevent self-suspension
        if (user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Cannot suspend admin users");
        }

        user.setActive(false);
        user.setSuspendedAt(LocalDateTime.now());
        user.setSuspendedReason(request.getReason());

        User savedUser = userRepository.save(user);

        log.info("User suspended successfully: {}", userId);
        return adminMapper.toDetailDTO(savedUser);
    }

    /**
     * Activate user account
     */
    @Transactional
    public AdminUserDetailDTO activateUser(UUID userId) {
        log.info("Admin activating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setActive(true);
        user.setSuspendedAt(null);
        user.setSuspendedReason(null);

        User savedUser = userRepository.save(user);

        log.info("User activated successfully: {}", userId);
        return adminMapper.toDetailDTO(savedUser);
    }

    /**
     * Admin can update any user's information
     */
    @Transactional
    public AdminUserDetailDTO updateUser(UUID userId, UpdateUserRequest request) {
        log.info("Admin updating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        userMapper.updateEntityFromDTO(user, request);
        User savedUser = userRepository.save(user);

        log.info("User updated successfully by admin: {}", userId);
        return adminMapper.toDetailDTO(savedUser);
    }

    /**
     * Delete user (hard delete - use with caution)
     */
    @Transactional
    public void deleteUser(UUID userId) {
        log.warn("Admin deleting user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Prevent deleting admin users
        if (user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Cannot delete admin users");
        }

        userRepository.delete(user);
        log.warn("User deleted successfully: {}", userId);
    }

    /**
     * Get system statistics for admin dashboard
     */
    public AdminStatsDTO getSystemStats() {
        log.debug("Admin fetching system statistics");

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActive(true);
        long suspendedUsers = userRepository.countByIsActive(false);
        long homelessUsers = userRepository.countByIsHomeless(true);

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long newUsersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long newUsersToday = userRepository.countByCreatedAtAfter(startOfDay);

        // Get user distribution by branch
        Map<String, Long> usersByBranch = new HashMap<>();
        for (BranchOfService branch : BranchOfService.values()) {
            long count = userRepository.countByBranchOfService(branch);
            usersByBranch.put(branch.getDisplayName(), count);
        }

        // Get user distribution by state (top 10)
        Map<String, Long> usersByState = userRepository.findAll().stream()
                .filter(user -> user.getState() != null)
                .collect(Collectors.groupingBy(User::getState, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return AdminStatsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .suspendedUsers(suspendedUsers)
                .homelessUsers(homelessUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .newUsersToday(newUsersToday)
                .usersByBranch(usersByBranch)
                .usersByState(usersByState)
                .build();
    }
}