package cgx.com.usecase.Interface_Common;

/**
 * Interface cho dịch vụ tạo một chuỗi Token
 * an toàn, ngẫu nhiên (ví dụ: 32 ký tự).
 * Đây là token "plain-text" sẽ được gửi qua email.
 */
public interface ISecureTokenGenerator {
    String generate();
}