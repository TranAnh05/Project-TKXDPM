package usecase.ManageUser;

import java.util.List;

import usecase.ManageUser.SearchUsers.UserSearchCriteria;

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
    
    /**
     * CẬP NHẬT một người dùng đã tồn tại (MỚI).
     * @param userData DTO chứa thông tin người dùng đã cập nhật.
     * @return UserData DTO sau khi đã cập nhật.
     */
    UserData update(UserData userData);
    
    /**
     * TÌM KIẾM user theo tiêu chí (MỚI).
     * @param criteria DTO chứa các tiêu chí (ví dụ: email, name)
     * @param pageNumber Trang hiện tại (ví dụ: 0)
     * @param pageSize Số lượng mục mỗi trang (ví dụ: 10)
     * @return Danh sách UserData DTOs
     */
    List<UserData> search(UserSearchCriteria criteria, int pageNumber, int pageSize);

    /**
     * ĐẾM số lượng user theo tiêu chí (MỚI).
     * (Cần thiết cho việc phân trang).
     * @param criteria DTO chứa các tiêu chí
     * @return Tổng số user khớp
     */
    long count(UserSearchCriteria criteria);
}
