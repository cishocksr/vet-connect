package com.vetconnect.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * System statistics for admin dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {
    private long totalUsers;
    private long activeUsers;
    private long suspendedUsers;
    private long homelessUsers;
    private long newUsersThisMonth;
    private long newUsersToday;
    private Map<String, Long> usersByBranch;
    private Map<String, Long> usersByState;
}