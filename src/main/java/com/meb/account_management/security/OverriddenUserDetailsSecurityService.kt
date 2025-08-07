package com.meb.account_management.security

import com.meb.account_management.model.CustomUser
import com.meb.account_management.repository.CustomUserRepository
import lombok.AllArgsConstructor
import lombok.experimental.UtilityClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
@AllArgsConstructor
class OverriddenUserDetailsSecurityService : UserDetailsService {

    @Autowired
    private lateinit var customUserRepository: CustomUserRepository

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(userId: String): UserDetails {
        val userId = userId.toLong()
        val user = customUserRepository.findById(userId)
        if (user.isPresent) {
            val presentUser: CustomUser = user.get()
            return SecurityUtils.convert(presentUser)
        }
        throw UsernameNotFoundException("User not found for ID: $userId")
    }
}

@UtilityClass
class SecurityUtils {
    companion object {
        fun convert(user: CustomUser): User {
            val rolse = user.roles
            val authorities = rolse.map { role -> SimpleGrantedAuthority(role.toString()) }
            val userInformation = user.userInformation
            return User(userInformation.username,user.password, authorities)
        }
    }
}
