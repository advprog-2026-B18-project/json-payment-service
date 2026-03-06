package id.ac.ui.cs.advprog.jsonpaymentservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret:change-me}")
    private String jwtSecret;

    @Getter
    @Value("${app.jwt.expiration-ms:900000}")
    private long jwtExpirationMs;

    public Claims extractClaims(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }

    public String getAccountIdFromToken(String token) {
        return extractClaims(token).getSubject();
    }

    public String getEmailFromToken(String token) {
        return extractClaims(token).get("email", String.class);
    }

    public String getRoleFromToken(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}