package com.meb.account_management.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.AllArgsConstructor
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.UUID

@Component
@AllArgsConstructor
class JwtAuthenticationFilter : OncePerRequestFilter() {

    @Autowired
    private lateinit var jwtUtils: JwtUtils

    @Autowired
    private lateinit var overriddenUserDetailsSecurityService: OverriddenUserDetailsSecurityService

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
    ) {

        val authorizationHeader = request.getHeader("Authorization")
        if (authorizationHeader != null) {
            if (authorizationHeader.startsWith("Bearer ")) {
                val token = authorizationHeader.substring(7)
                val userId = jwtUtils!!.extractUserId(token)
                if (userId != null ) {
                    val userDetails = overriddenUserDetailsSecurityService?.loadUserByUsername(userId)
                    if(userDetails!= null){
                        if (jwtUtils.validateToken(token, userDetails)) {
                            val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.authorities
                            )

                            usernamePasswordAuthenticationToken.details =
                                    WebAuthenticationDetailsSource().buildDetails(request)
                            SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
                        }
                    }
                }
            }
        }
        filterChain.doFilter(request, response)
    }
}