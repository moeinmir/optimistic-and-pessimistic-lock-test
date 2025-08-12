package com.meb.account_management.service.impl;

import com.meb.account_management.aop.LogExecutionTime;
import com.meb.account_management.dto.LoginResponseDto;
import com.meb.account_management.dto.ServiceResponse;
import com.meb.account_management.model.CustomUser;
import com.meb.account_management.repository.CustomUserRepository;
import com.meb.account_management.security.JwtUtils;
import com.meb.account_management.service.UserService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.slf4j.MDC;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final CustomUserRepository customUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserServiceImpl(CustomUserRepository customUserRepository,  PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.customUserRepository = customUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public ServiceResponse<CustomUser> getUserByUserName(String username) {
        Optional<CustomUser> optionalCustomUser = customUserRepository.findByUsername(username);
        return ServiceResponse.fromOptional(optionalCustomUser);
    }

    @Override
    public ServiceResponse<CustomUser> register(String firstName,String lastname, String username, String password) {
        val existingUser = getUserByUserName(username);
        if (existingUser.isSuccess()){
            return ServiceResponse.failure();
        }
        CustomUser user = CustomUser.createNewUser(firstName,lastname, username, passwordEncoder.encode(password));
        customUserRepository.save(user);
        return ServiceResponse.success(user);
    }

    @Override
    public  ServiceResponse<LoginResponseDto> login(String userName, String password){
        val requestId = MDC.get("requestId");
        val fetchedUser = getUserByUserName(userName);
        if (fetchedUser.isSuccess()){
            val user = fetchedUser.getResult();
            if (user.doesPasswordMatch(password,passwordEncoder)){
                val accessToken = jwtUtils.generateAccessToken(user);
                val refreshToken = jwtUtils.generateRefreshToken(user);
                return ServiceResponse.success(LoginResponseDto.builder().accessToken(accessToken).refreshToken(refreshToken).build());
            }
        }
        return ServiceResponse.failure();
    }

    @LogExecutionTime
    @Override
    public ServiceResponse<CustomUser.UserDto> getUserInformationWithUsername(String userName) {
        val fetchedUser = getUserByUserName(userName);
        if (fetchedUser.isSuccess()){
            val userInfo = fetchedUser.getResult().getUserInformation();
            return ServiceResponse.success(userInfo);
        }
        return ServiceResponse.failure();
    }
}

