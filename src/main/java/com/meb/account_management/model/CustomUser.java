package com.meb.account_management.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Getter
public class CustomUser  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String username;

    public String password;

    public boolean doesPasswordMatch(String password, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches( password, this.password );
    }

    @Setter
    @Builder
    @Getter
    public static class UserDto{
        public Long id;
        public String fistName;
        public String lastName;
        public String username;
    }

    public static CustomUser createNewUser(String firstName,String lastName, String username, String encodedPassword){
        return CustomUser.builder().firstName(firstName).lastName(lastName).username(username).password(encodedPassword).build();
    }

    public UserDto getUserInformation(){
        return UserDto.builder().id(this.id).fistName(this.firstName).lastName(this.lastName).username(this.username).build();
    }

}
