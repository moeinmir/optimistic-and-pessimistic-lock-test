package com.meb.account_management.security

import com.meb.account_management.model.CustomUser
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import lombok.AllArgsConstructor
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey

@Component
@AllArgsConstructor
class JwtUtils() {

    @Value("\${jwt.secret-key}")
    private val jwtSecretKey: String? = null

    @Value("\${jwt.access-token-expiration-time}")
    private val accessTokenExpirationTime: Int? = null

    @Value("\${jwt.refresh-token-expiration-time}")
    private val refreshTokenExpirationTime: Int? = null


    private fun getSigningKey(): SecretKey {
        val keyBytes = Decoders.BASE64.decode(jwtSecretKey)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun extractUserId(token: String): String {
        return (extractAllClaims(token)?.get("user_id") ?: "") as String
    }

    fun extractUsername(token: String): String {
        return (extractAllClaims(token)?.get("username") ?: "") as String
    }

    fun extractExpiration(token: String): Date {
        return extractClaim<Date>(token, Claims::getExpiration)
    }

    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims: Claims? = extractAllClaims(token)
        return claimsResolver(claims!!)
    }

    private fun extractAllClaims(token: String): Claims? {
        try {
            return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token.replace("Bearer ", ""))
                .getPayload();
        }
        catch (ex: Exception){
            throw Exception("Error extracting claims from token", ex)
        }
    }

    fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    fun generateAccessToken(user: CustomUser): String {
        val claims: MutableMap<String, Any> = HashMap()
        val userInformation = user.userInformation
        val userId: Long = userInformation.id
        claims["user_id"] = userId.toString();
        val username = userInformation.username;
        claims["username"] = username;
        return createToken(claims, username, TimeUnit.HOURS.toMillis(accessTokenExpirationTime?.toLong()  ?: 0))
    }

    fun generateRefreshToken(user: CustomUser): String {
        val claims: Map<String, Any> = HashMap()
        val username: String = user.userInformation.username
        return createToken(claims, username, TimeUnit.DAYS.toMillis(refreshTokenExpirationTime?.toLong() ?: 0))
    }

    private fun createToken(claims: Map<String, Any>, subject: String, expiration: Long): String {
        return Jwts.builder().claims(claims).subject(subject).issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expiration)).signWith(getSigningKey()).compact();
    }

    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }
}