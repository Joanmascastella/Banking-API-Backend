package com.BankingAPI.BankingAPI.Group1.util;


import com.BankingAPI.BankingAPI.Group1.config.BeanFactory;
import com.BankingAPI.BankingAPI.Group1.model.Enums.UserType;
import com.BankingAPI.BankingAPI.Group1.service.MemberDetailsService;
import io.jsonwebtoken.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    @Value("${application.token.validity}")
    private long validityInMicroseconds;
    private final MemberDetailsService memberDetailsService;
    private final JwtKeyProvider jwtKeyProvider;
    private final BeanFactory beanFactory;

    public JwtTokenProvider(MemberDetailsService memberDetailsService, JwtKeyProvider jwtKeyProvider, BeanFactory beanFactory) {
        this.memberDetailsService = memberDetailsService;
        this.jwtKeyProvider = jwtKeyProvider;
        this.beanFactory = beanFactory;
    }

    public String createToken(String username, long userId, List<UserType> roles) throws JwtException {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userId", userId);
        claims.put("auth",
                roles
                        .stream()
                        .map(UserType::name)
                        .toList());


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
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(jwtKeyProvider.getPrivateKey()).build().parseClaimsJws(token);
            String username = claims.getBody().getSubject();
            UserDetails userDetails = memberDetailsService.loadUserByUsername(username);
            return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Bearer token not valid");
        }
    }

}
