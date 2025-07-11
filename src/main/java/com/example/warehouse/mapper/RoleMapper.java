package com.example.warehouse.mapper;

import com.example.warehouse.entity.Role;
import com.example.warehouse.payload.response.RoleResponse;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public RoleResponse toRoleResponse(Role role) {
        if (role == null) {
            return null;
        }

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }
}
