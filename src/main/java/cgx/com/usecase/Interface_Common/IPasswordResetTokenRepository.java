package cgx.com.usecase.Interface_Common;

/**
 * Interface cho Repository lưu trữ PasswordResetToken.
 * (Tách biệt với IUserRepository).
 */
public interface IPasswordResetTokenRepository {
    /**
     * Lưu một token mới vào CSDL.
     * @param tokenData DTO chứa thông tin token.
     */
    void save(PasswordResetTokenData tokenData);
    
    /**
     * TÌM một token bằng giá trị HASH của nó (MỚI).
     * @param hashedToken Token đã được băm
     * @return PasswordResetTokenData nếu tìm thấy, ngược lại null.
     */
    PasswordResetTokenData findByHashedToken(String hashedToken);
    
    /**
     * XÓA một token bằng ID của nó (MỚI).
     * (Được gọi sau khi token đã được sử dụng thành công).
     * @param tokenId ID của bản ghi token cần xóa
     */
    void deleteByTokenId(String tokenId);
}