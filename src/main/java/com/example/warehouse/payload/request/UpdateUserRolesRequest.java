package com.example.warehouse.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Set;

@Data
public class UpdateUserRolesRequest {

    @NotNull(message = "Role IDs set cannot be null")
    private Set<Integer> roleIds;
}
