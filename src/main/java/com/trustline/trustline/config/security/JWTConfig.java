package com.trustline.trustline.config.security;

import com.google.api.client.util.Value;
import com.trustline.trustline.appuser.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JWTConfig {

    @Value("${jwt.signin.secret}")
    private String secret;


private SecretKey setKey(){
   return Keys.secretKeyFor(SignatureAlgorithm.HS256);
}

    public String generateToken(User user){
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("roles", user.getRoles())
                .claim("status", user.getStatus())
                .claim("id", user.getId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 60 *60 * 30))
                .signWith(setKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String extractEmail(String token){
        return extractClaims(token).getSubject();
    }
    private Claims extractClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractEmail(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }


    private Boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date(System.currentTimeMillis()));
    }

}
