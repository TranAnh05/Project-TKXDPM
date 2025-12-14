package cgx.com.infrastructure.security;

import java.util.UUID;

import org.springframework.stereotype.Component;

import cgx.com.usecase.ManageUser.IUserIdGenerator;

@Component
public class UuidUserIdGenerator implements IUserIdGenerator {
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}