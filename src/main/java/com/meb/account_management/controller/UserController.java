package com.meb.account_management.controller;

import com.meb.account_management.dto.LoginRequestDto;
import com.meb.account_management.dto.LoginResponseDto;
import com.meb.account_management.dto.RegisterRequestDto;
import com.meb.account_management.dto.ServiceResponse;
import com.meb.account_management.model.CustomUser;
import com.meb.account_management.repository.CustomUserRepository;
import com.meb.account_management.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.catalina.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/")
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("register")
    public ServiceResponse<CustomUser.UserDto> registerMe(@RequestBody RegisterRequestDto registerRequestDto){
        val response = userService.register(registerRequestDto.getFirsName(),registerRequestDto.getLastName(),registerRequestDto.getUsername(), registerRequestDto.getPassword());
        if(response.isSuccess()){
            return ServiceResponse.success(response.getResult().getUserInformation());
        }
        return response.failure();
    }

    @PostMapping("login")
    public ServiceResponse<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto){
        return userService.login(loginRequestDto.getUserName(),loginRequestDto.getPassword());
    }

    @GetMapping("info")
    public ServiceResponse<CustomUser.UserDto> getMyInformation(){
        val username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserInformationWithUsername(username);
    }
}
