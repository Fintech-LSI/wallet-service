package com.fintech.walletservice.dto.responses;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    // Add other fields if necessary
}