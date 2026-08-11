package com.busgo.user.dto;

import java.util.List;
import java.util.UUID;

public class UserDto {
    private UUID id;
    private String fullName;
    private String email;
    private String mobile;
    private List<String> roles;

    public UserDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
