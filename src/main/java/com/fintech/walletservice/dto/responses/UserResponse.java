package com.fintech.walletservice.dto.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    // Add other fields if necessary
}
