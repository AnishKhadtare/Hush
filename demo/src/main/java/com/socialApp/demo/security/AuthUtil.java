package com.socialApp.demo.security;

import com.socialApp.demo.dto.response.JwtResponse;
import com.socialApp.demo.entity.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

@Component
public class AuthUtil {
    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    public String generateToken(Users user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() +1000*60*60*24))
                .signWith(generateKey())
                .compact();
    }

    public JwtResponse verifyJwtToken(String token){
        Claims claims= Jwts.parser()
                            .verifyWith(generateKey())
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

        Long userId = Long.parseLong(claims.get("userId", String.class));
        String username = claims.getSubject();
        return new JwtResponse(userId, username, new ArrayList<>());
    }

    public SecretKey generateKey(){
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    public Long getCurrentLoggedInUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !(authentication.getPrincipal() instanceof JwtResponse jwtResponse)){
            throw new AuthenticationCredentialsNotFoundException("Jwt credentials not found");
        }
        return jwtResponse.userId();
    }
}
