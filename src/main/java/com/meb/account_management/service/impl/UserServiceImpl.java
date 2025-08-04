package com.meb.account_management.service.impl;

import com.meb.account_management.dto.LoginResponseDto;
import com.meb.account_management.model.CustomUser;
import com.meb.account_management.repository.CustomUserRepository;
import com.meb.account_management.service.UserService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.meb.account_management.security.JwtUtils;

import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final CustomUserRepository customUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    public UserServiceImpl(CustomUserRepository customUserRepository) {
        this.customUserRepository = customUserRepository;
    }

    @Override
    public CustomUser register(String firstName,String lastname, String username, String password) {
        CustomUser user = CustomUser.createNewUser(firstName,lastname, username, passwordEncoder.encode(password));
        customUserRepository.save(user);
        return user;
    }

    public Optional<CustomUser> findByUserName(String username) {
        return customUserRepository.findByUsername(username);
    }

    @Override
    public LoginResponseDto login(String userName, String password){
        val user = findByUserName(userName);
        if (user.isPresent()){
            val fetchedUser = user.get();
            if (fetchedUser.doesPasswordMatch(password,passwordEncoder)){
                val accessToken = jwtUtils.generateAccessToken(fetchedUser);
                val refreshToken = jwtUtils.generateRefreshToken(fetchedUser);
                return LoginResponseDto.builder().accessToken(accessToken).refreshToken(refreshToken).build();
            }
        }
        return LoginResponseDto.builder().build();
    }

    @Override
    public CustomUser.UserDto getUserInformationWithUsername(String userName) {
        val user = findByUserName(userName);
        return user.map(CustomUser::getUserInformation).orElse(null);
    }
}

