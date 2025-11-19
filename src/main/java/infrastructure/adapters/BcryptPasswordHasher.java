package infrastructure.adapters;

import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import usecase.ManageUser.IPasswordHasher;

/**
 * Triển khai IPasswordHasher bằng thuật toán BCrypt.
 */
@Component
public class BcryptPasswordHasher implements IPasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String plainText) {
        return encoder.encode(plainText);
    }

    @Override
    public boolean verify(String plainText, String hashedText) {
        return encoder.matches(plainText, hashedText);
    }
}