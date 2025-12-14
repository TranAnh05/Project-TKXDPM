package cgx.com.infrastructure.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import cgx.com.usecase.Interface_Common.IPasswordHasher;

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
