package com.vetconnect.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspendUserRequest {

    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason must be less than 500 characters")
    private String reason;
}