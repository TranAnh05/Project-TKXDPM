package cgx.com.infrastructure.external;

import org.springframework.stereotype.Component;

import cgx.com.usecase.Interface_Common.IEmailService;

@Component
public class FakeEmailService implements IEmailService {

    @Override
    public void sendVerificationEmail(String toEmail, String userName, String verificationToken) {
        System.out.println("--- [FAKE EMAIL] Gửi Email Xác Thực ---");
        System.out.println("To: " + toEmail);
        System.out.println("Content: Xin chào " + userName + ", mã xác thực của bạn là: " + verificationToken);
        System.out.println("---------------------------------------");
    }

    @Override
    public void sendAccountCreatedEmail(String toEmail, String userName, String initialPassword) {
        System.out.println("--- [FAKE EMAIL] Account Created ---");
        System.out.println("Pass: " + initialPassword);
    }
    
    // Implement rỗng các hàm khác để không báo lỗi
    @Override public void sendPasswordResetEmail(String t, String u, String token) {}
    @Override public void sendPasswordChangeAlert(String t, String u) {}
}
