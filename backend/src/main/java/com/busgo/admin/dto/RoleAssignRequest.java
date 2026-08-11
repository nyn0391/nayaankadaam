package com.busgo.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class RoleAssignRequest {
    @NotBlank
    private String roleName;

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}
