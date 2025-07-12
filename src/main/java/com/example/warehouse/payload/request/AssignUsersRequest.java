package com.example.warehouse.payload.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AssignUsersRequest {

    @NotEmpty(message = "User IDs list cannot be empty.")
    private List<Integer> userIds;
}
