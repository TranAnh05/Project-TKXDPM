package cgx.com.infrastructure.adapters;

import java.util.UUID;

import org.springframework.stereotype.Component;

import cgx.com.usecase.ManageCategory.ICategoryIdGenerator;
import cgx.com.usecase.ManageUser.IPasswordResetTokenIdGenerator;
import cgx.com.usecase.ManageUser.ISecureTokenGenerator;
import cgx.com.usecase.ManageUser.IUserIdGenerator;

/**
 * Triển khai chung cho việc tạo ID bằng UUID.
 * Một class này có thể implement nhiều interface generator khác nhau
 * vì logic tạo ID ngẫu nhiên là giống nhau.
 */
@Component
public class UuidGenerator implements IUserIdGenerator, ICategoryIdGenerator, IPasswordResetTokenIdGenerator, ISecureTokenGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}