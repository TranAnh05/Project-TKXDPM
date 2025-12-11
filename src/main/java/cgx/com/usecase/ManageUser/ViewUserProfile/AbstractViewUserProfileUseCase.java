package cgx.com.usecase.ManageUser.ViewUserProfile;

import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public abstract class AbstractViewUserProfileUseCase implements ViewUserProfileInputBoundary{
	protected final IAuthTokenValidator tokenValidator;
    protected final IUserRepository userRepository;
    protected final ViewUserProfileOutputBoundary outputBoundary;
    
    public AbstractViewUserProfileUseCase(IAuthTokenValidator tokenValidator,
            IUserRepository userRepository,
            ViewUserProfileOutputBoundary outputBoundary) {
		this.tokenValidator = tokenValidator;
		this.userRepository = userRepository;
		this.outputBoundary = outputBoundary;
	}
    
    @Override
    public final void execute(ViewUserProfileRequestData input) {
        ViewUserProfileResponseData output = new ViewUserProfileResponseData();

        try {
            // 1. Kiểm tra input (Chung)
            if (input.authToken == null || input.authToken.trim().isEmpty()) {
                throw new SecurityException("Auth Token không được để trống.");
            }

            // 2. Xác thực Token (Chung)
            // Lấy "danh tính" (Principal) từ token
            AuthPrincipal principal = tokenValidator.validate(input.authToken);

            // 3. Lấy ID Người dùng (Riêng - Concrete)
            // Lớp Con sẽ quyết định xem user ID nào (ví dụ: 'me' hoặc 'specific_id')
            String userIdToView = getUserIdToView(principal, input);

            // 4. Kiểm tra nghiệp vụ (Chung)
            UserData userData = userRepository.findByUserId(userIdToView);
            
            if (userData == null) {
                // Token hợp lệ, nhưng user không còn tồn tại (ví dụ: đã bị xóa)
                throw new SecurityException("Không tìm thấy người dùng.");
            }
            
            // 5. Báo cáo thành công (Chung)
            output.success = true;
            output.message = "Lấy thông tin thành công.";
            output.userId = userData.userId;
            output.email = userData.email;
            output.firstName = userData.firstName;
            output.lastName = userData.lastName;
            output.phoneNumber = userData.phoneNumber;

        } catch (SecurityException e) {
            // 6. BẮT LỖI NGHIỆP VỤ / BẢO MẬT (T3)
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            // 7. Bắt lỗi hệ thống
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống không xác định.";
        }

        // 8. Trình bày kết quả (Chung)
        outputBoundary.present(output);
    }
    
    /**
     * Lớp Con (Concrete) phải implement hàm này để
     * quyết định ID của user nào sẽ được xem.
     */
    protected abstract String getUserIdToView(AuthPrincipal principal, ViewUserProfileRequestData input);
}
