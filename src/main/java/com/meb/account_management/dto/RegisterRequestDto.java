package com.meb.account_management.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterRequestDto {
    private String firsName;
    private String password;
    private String username;
    private String lastName;
}
