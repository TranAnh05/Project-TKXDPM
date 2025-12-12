package cgx.com.usecase.Interface_Common;

/**
 * Interface cho một dịch vụ bên ngoài: Băm mật khẩu.
 * Logic nghiệp vụ không quan tâm nó là Bcrypt hay Argon2,
 * chỉ cần nó có thể băm và xác thực
 */
public interface IPasswordHasher {
	/**
     * Băm một mật khẩu (dùng khi đăng ký).
     * @param plainText Mật khẩu thô
     * @return Chuỗi đã được băm
     */
    String hash(String plainText);
    /**
     * Xác thực một mật khẩu thô với một chuỗi đã băm (dùng khi đăng nhập).
     * @param plainText Mật khẩu thô người dùng nhập
     * @param hashedText Chuỗi băm lấy từ CSDL
     * @return true nếu khớp, ngược lại false
     */
    boolean verify(String plainText, String hashedText);
}
