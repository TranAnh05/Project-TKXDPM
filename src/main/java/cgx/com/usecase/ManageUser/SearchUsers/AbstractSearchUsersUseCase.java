package cgx.com.usecase.ManageUser.SearchUsers;

import java.util.List;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public abstract class AbstractSearchUsersUseCase implements SearchUsersInputBoundary{
	protected final IAuthTokenValidator tokenValidator;
    protected final IUserRepository userRepository;
    protected final SearchUsersOutputBoundary outputBoundary;
    
    public AbstractSearchUsersUseCase(IAuthTokenValidator tokenValidator,
            IUserRepository userRepository,
            SearchUsersOutputBoundary outputBoundary) {
		this.tokenValidator = tokenValidator;
		this.userRepository = userRepository;
		this.outputBoundary = outputBoundary;
	}
    
    
	@Override
	public void execute(SearchUsersRequestData input) {
		SearchUsersResponseData output = new SearchUsersResponseData();

        try {
            // 1. Kiểm tra input (Token)
            if (input.authToken == null || input.authToken.trim().isEmpty()) {
                throw new SecurityException("Auth Token không được để trống.");
            }

            // 2. Xác thực Token & Phân quyền (Authorization)
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            if (principal.role != UserRole.ADMIN) {
                throw new SecurityException("Không có quyền truy cập.");
            }
            
            // 3. Xây dựng Tiêu chí Tìm kiếm (Riêng - Concrete)
            UserSearchCriteria criteria = buildSearchCriteria(input);

            // 4. Lấy dữ liệu (Chung)
            // CSDL thường dùng page index (0-based), UI dùng page number (1-based)
            int pageIndex = input.pageNumber - 1; 
            List<UserData> users = userRepository.search(criteria, pageIndex, input.pageSize);
            
            // 5. Lấy tổng số lượng (Chung)
            long totalCount = userRepository.count(criteria);
            
            // 6. Tạo DTO Phân trang (Chung)
            PaginationData pagination = new PaginationData(totalCount, input.pageNumber, input.pageSize);

            // 7. Báo cáo thành công (Chung)
            output.success = true;
            output.message = "Tìm kiếm thành công.";
            output.users = users;
            output.pagination = pagination;

        } catch (SecurityException e) {
            // 8. BẮT LỖI NGHIỆP VỤ / BẢO MẬT (T3)
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            // 9. Bắt lỗi hệ thống
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống không xác định.";
        }

        // 10. Trình bày kết quả (Chung)
        outputBoundary.present(output);
	}


	protected abstract UserSearchCriteria buildSearchCriteria(SearchUsersRequestData input);
}
