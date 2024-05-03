package com.BankingAPI.BankingAPI.Group1.util;


import com.BankingAPI.BankingAPI.Group1.config.BeanFactory;
import com.BankingAPI.BankingAPI.Group1.model.Enums.UserType;
import com.BankingAPI.BankingAPI.Group1.service.MemberDetailsService;
import io.jsonwebtoken.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${application.token.validity}")
    private long validityInMicroseconds;
    private final JwtKeyProvider jwtKeyProvider;

    public JwtTokenProvider( JwtKeyProvider jwtKeyProvider) {
        this.jwtKeyProvider = jwtKeyProvider;
    }

    public String createToken(String username, long userId, UserType type) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userId", userId);
        claims.put("auth", type.name());

        Date now = new Date();
        Date expiration = new Date(now.getTime() + validityInMicroseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(jwtKeyProvider.getPrivateKey())
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(jwtKeyProvider.getPrivateKey())
                .build()
                .parseClaimsJws(token);

        Claims body = claims.getBody();
        String username = body.getSubject();
        String authority = body.get("auth", String.class);
        Long userId = body.get("userId", Long.class);

        List<SimpleGrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority(authority));
        CustomUserDetails userDetails =
                new CustomUserDetails(username, "", userId, authorities);

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

}
