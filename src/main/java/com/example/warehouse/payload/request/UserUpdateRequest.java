package com.example.warehouse.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UserUpdateRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;

    @NotEmpty(message = "User must have at least one role")
    private Set<String> roles;

    private Boolean isActive;
}
