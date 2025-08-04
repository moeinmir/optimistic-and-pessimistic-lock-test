package com.meb.account_management.controller;

import com.meb.account_management.dto.LoginRequestDto;
import com.meb.account_management.dto.LoginResponseDto;
import com.meb.account_management.dto.RegisterRequestDto;
import com.meb.account_management.model.CustomUser;
import com.meb.account_management.repository.CustomUserRepository;
import com.meb.account_management.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/")
public class UserController {

    private final UserService userService;
    private final CustomUserRepository customUserRepository;

    @PostMapping("register")
    public CustomUser.UserDto registerMe(@RequestBody RegisterRequestDto registerRequestDto){
        CustomUser user = userService.register(registerRequestDto.getFirsName(),registerRequestDto.getLastName(),registerRequestDto.getUsername(), registerRequestDto.getPassword());
        return user.getUserInformation();
    }

    @PostMapping("login")
    public LoginResponseDto login(@RequestBody LoginRequestDto loginRequestDto){
        return userService.login(loginRequestDto.getUserName(),loginRequestDto.getPassword());
    }

    @GetMapping("info")
    public CustomUser.UserDto getMyInformation(){
        val username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserInformationWithUsername(username);
    }

}
