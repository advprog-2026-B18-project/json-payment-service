package id.ac.ui.cs.advprog.jsonpaymentservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private PrivateKey testPrivateKey;

    @BeforeEach
    void setUp() throws Exception {
        // Generate a temporary, on-the-fly RSA Key Pair for testing
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        
        testPrivateKey = keyPair.getPrivate();
        PublicKey testPublicKey = keyPair.getPublic();

        // Base64 encode the public key to mimic .env file
        String base64PublicKey = Encoders.BASE64.encode(testPublicKey.getEncoded());

        // Initialize the utility and inject the public key string using reflection
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "publicKeyString", base64PublicKey);
    }

    private String generateTestToken(String userId, String username, String role, long expirationMillis) {
        return Jwts.builder()
                .claim("user_id", userId)
                .claim("username", username)
                .claim("role", role)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(testPrivateKey) // Sign the token using the test PRIVATE key
                .compact();
    }

    @Test
    void shouldExtractAllClaimsCorrectly() {
        String expectedUserId = UUID.randomUUID().toString();
        String expectedUsername = "car_enthusiast_99";
        String expectedRole = "CUSTOMER";
        
        // generate a token valid for 1 hour
        String token = generateTestToken(expectedUserId, expectedUsername, expectedRole, 3600000);

        String actualUserId = jwtUtil.extractUserId(token);
        String actualUsername = jwtUtil.extractUsername(token);
        String actualRole = jwtUtil.extractRole(token);

        assertEquals(expectedUserId, actualUserId);
        assertEquals(expectedUsername, actualUsername);
        assertEquals(expectedRole, actualRole);
    }

    @Test
    void shouldReturnTrueForValidToken() {
        String token = generateTestToken(UUID.randomUUID().toString(), "admin_user", "ADMIN", 3600000);

        boolean isValid = jwtUtil.isTokenValid(token);

        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalseForExpiredToken() {
        // generate an expired token
        String expiredToken = generateTestToken(UUID.randomUUID().toString(), "test_user", "USER", -1000);

        boolean isValid = jwtUtil.isTokenValid(expiredToken);

        assertFalse(isValid);
    }

    @Test
    void shouldReturnFalseForTamperedToken() throws Exception {
        String token = generateTestToken(UUID.randomUUID().toString(), "test_user", "USER", 3600000);
        
        // Tamper with the token (change the payload slightly without updating the signature)
        // Standard JWTs have 3 parts separated by dots. We alter the payload (middle part)
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + "tampered." + parts[2];

        boolean isValid = jwtUtil.isTokenValid(tamperedToken);

        assertFalse(isValid);
    }
}
