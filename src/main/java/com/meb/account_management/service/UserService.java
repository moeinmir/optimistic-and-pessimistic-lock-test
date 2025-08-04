package com.meb.account_management.service;

import com.meb.account_management.dto.LoginResponseDto;
import com.meb.account_management.dto.ServiceResponse;
import com.meb.account_management.model.CustomUser;

public interface UserService {
    ServiceResponse<CustomUser> getUserByUserName(String username);
    ServiceResponse<CustomUser> register(String firstName,String lastname, String username, String password);
    ServiceResponse<LoginResponseDto> login(String userName, String password);
    ServiceResponse<CustomUser.UserDto> getUserInformationWithUsername(String userName);

}
