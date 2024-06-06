package com.BankingAPI.BankingAPI.Group1.util;


import com.BankingAPI.BankingAPI.Group1.model.Enums.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    @Value("${application.token.validity}")
    private long validityInMicroseconds;
    private final JwtKeyProvider jwtKeyProvider;

    public JwtTokenProvider( JwtKeyProvider jwtKeyProvider) {
        this.jwtKeyProvider = jwtKeyProvider;
    }

    public String createToken(Long userId, UserType type, Boolean isApproved) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("auth", type.name());
        claims.put("approved", String.valueOf(isApproved));

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
        Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(jwtKeyProvider.getPrivateKey()).build().parseClaimsJws(token);
        String userID = claims.getBody().getSubject();
        String authority = claims.getBody().get("auth", String.class);
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(authority));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(userID, "", authorities);
        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }
}
