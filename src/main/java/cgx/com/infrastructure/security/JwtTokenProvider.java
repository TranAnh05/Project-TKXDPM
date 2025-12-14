package cgx.com.infrastructure.security;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenGenerator;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;

@Component
public class JwtTokenProvider implements IAuthTokenGenerator, IAuthTokenValidator {

    private final SecretKey key;
    private final long expiration;

    // Lấy cấu hình từ application.properties
    public JwtTokenProvider(@Value("${jwt.secret}") String secret, 
                            @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    // --- IMPLEMENT: IAuthTokenGenerator (Tạo Token) ---
    @Override
    public String generate(String userId, String email, UserRole role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(userId) // Lưu ID vào Subject
                .claim("email", email) // Lưu Email vào Claim
                .claim("role", role.name()) // Lưu Role vào Claim
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // --- IMPLEMENT: IAuthTokenValidator (Giải mã Token) ---
    @Override
    public AuthPrincipal validate(String token) throws SecurityException {
        try {
            // Loại bỏ tiền tố "Bearer " nếu có
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String userId = claims.getSubject();
            String email = claims.get("email", String.class);
            String roleStr = claims.get("role", String.class);
            UserRole role = UserRole.valueOf(roleStr);

            return new AuthPrincipal(userId, email, role);

        } catch (ExpiredJwtException e) {
            throw new SecurityException("Token đã hết hạn.");
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new SecurityException("Token không hợp lệ.");
        }
    }
}
