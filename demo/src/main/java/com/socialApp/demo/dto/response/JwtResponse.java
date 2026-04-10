package com.socialApp.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

public record JwtResponse(
        Long userId,
        String username,
        List<GrantedAuthority> authorities
) implements Principal {  // ✅ add this

    @Override
    public String getName() {
        return username;  // ✅ required by Principal interface
    }
}
