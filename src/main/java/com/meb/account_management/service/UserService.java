package com.meb.account_management.service;

import com.meb.account_management.dto.LoginResponseDto;
import com.meb.account_management.model.CustomUser;

public interface UserService {
    CustomUser register(String firstName,String lastname, String username, String password);
    LoginResponseDto login(String userName, String password);
    CustomUser.UserDto getUserInformationWithUsername(String userName);
}
