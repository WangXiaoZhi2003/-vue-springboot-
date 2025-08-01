package com.code.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * TODO
 *
 * @author zlong
 * @version 1.0
 * @date 2025-06-26 12:55
 */
@Component
public class JwtUtil {

    private final String SECRET = "6DkWzVzVZqYjJmYTBkMjM0ZTEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNA==";

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    public String getEmail(String token) {
        if (token == null) {
            System.out.println("Token is null");
            return null;
        }

        token = token.replace("Bearer ", "");


        try {
            String email = Jwts.parser().setSigningKey(SECRET)
                    .parseClaimsJws(token).getBody().getSubject();

            return email;
        } catch (Exception e) {
            System.err.println("Token 解析失败: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return null;
        }
    }


}
