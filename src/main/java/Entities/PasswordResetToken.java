package Entities;

import java.time.Duration;
import java.time.Instant;

/**
* Lớp Entity: PasswordResetToken
* Đại diện cho một token (mã) tạm thời, dùng một lần để cho phép
* người dùng đặt lại mật khẩu khi họ quên.
* Đây cũng là một POJO thuộc Layer 4.
*/
public class PasswordResetToken {

   private String tokenId; // ID của bản ghi token
   private String hashedToken; // Token đã được băm (để bảo mật)
   private String userId; // Liên kết đến User
   private Instant expiresAt; // Thời gian hết hạn

   public PasswordResetToken(String tokenId, String hashedToken, String userId, Instant expiresAt) {
       this.tokenId = tokenId;
       this.hashedToken = hashedToken;
       this.userId = userId;
       this.expiresAt = expiresAt;
   }

   /**
    * Phương thức "Static Factory Method" để tạo Token mới.
    * @param userId ID của người dùng
    * @param hashedToken Token đã được băm (do Use Case cung cấp)
    * @param validityDuration Thời gian hiệu lực (ví dụ: 15 phút)
    * @return Một thực thể PasswordResetToken mới
    */
   public static PasswordResetToken create(String tokenId, String userId, String hashedToken, Duration validityDuration) {
       Instant now = Instant.now();
       return new PasswordResetToken(
           tokenId,
           hashedToken,
           userId,
           now.plus(validityDuration) // Đặt thời gian hết hạn
       );
   }

   /**
    * Quy tắc nghiệp vụ: Kiểm tra xem token đã hết hạn hay chưa.
    * @return true nếu đã hết hạn, ngược lại false.
    */
   public boolean isExpired() {
       return Instant.now().isAfter(this.expiresAt);
   }

   // --- Getters ---
   public String getTokenId() { return tokenId; }
   public String getHashedToken() { return hashedToken; }
   public String getUserId() { return userId; }
   public Instant getExpiresAt() { return expiresAt; }
}