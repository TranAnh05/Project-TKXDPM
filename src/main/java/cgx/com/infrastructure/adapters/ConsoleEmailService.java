package cgx.com.infrastructure.adapters;

import org.springframework.stereotype.Component;

import cgx.com.usecase.ManageUser.IEmailService;

@Component
public class ConsoleEmailService implements IEmailService {

    @Override
    public void sendPasswordResetEmail(String toEmail, String userName, String plainTextToken) {
        System.out.println("========== MOCK EMAIL SERVICE ==========");
        System.out.println("To: " + toEmail);
        System.out.println("Subject: Yêu cầu đặt lại mật khẩu");
        System.out.println("Hello " + userName + ",");
        System.out.println("Use this token to reset your password: " + plainTextToken);
        System.out.println("========================================");
    }
}