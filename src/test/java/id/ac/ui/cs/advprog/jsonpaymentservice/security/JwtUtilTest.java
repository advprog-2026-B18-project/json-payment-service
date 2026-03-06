package id.ac.ui.cs.advprog.jsonpaymentservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = Encoders.BASE64.encode(
            "my-super-secret-key-for-tests-123456".getBytes(StandardCharsets.UTF_8)
    );

    private JwtUtil jwtUtil;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 12345L);
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }

    private String generateToken(String userId, String email, String role, long expirationMillis) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(secretKey)
                .compact();
    }

    @Test
    void shouldExtractClaimsCorrectly() {
        String token = generateToken("user-123", "user@example.com", "ADMIN", 3600000);

        assertEquals("user-123", jwtUtil.getAccountIdFromToken(token));
        assertEquals("user@example.com", jwtUtil.getEmailFromToken(token));
        assertEquals("ADMIN", jwtUtil.getRoleFromToken(token));
    }

    @Test
    void shouldReturnTrueForValidToken() {
        String token = generateToken("user-123", "user@example.com", "ADMIN", 3600000);

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void shouldReturnFalseForExpiredToken() {
        String expiredToken = generateToken("user-123", "user@example.com", "ADMIN", -1000);

        assertFalse(jwtUtil.validateToken(expiredToken));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token"));
    }

    @Test
    void shouldExposeConfiguredExpiration() {
        assertEquals(12345L, jwtUtil.getJwtExpirationMs());
    }
}
