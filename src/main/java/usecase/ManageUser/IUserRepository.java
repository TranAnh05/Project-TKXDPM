package usecase.ManageUser;

public interface IUserRepository {
	/**
     * Tìm người dùng bằng email.
     * @return UserData DTO nếu tìm thấy, ngược lại null.
     */
    UserData findByEmail(String email);
    
    /**
     * Lưu một người dùng mới vào CSDL.
     * @param userData DTO chứa thông tin người dùng.
     * @return UserData DTO sau khi đã lưu.
     */
    UserData save(UserData userData);
    
    /**
     * TÌM NGƯỜI DÙNG BẰNG ID.
     * (Cần thiết cho việc xem hồ sơ và các thao tác xác thực khác).
     * @param userId ID của người dùng
     * @return UserData DTO nếu tìm thấy, ngược lại null.
     */
    UserData findByUserId(String userId);
}
