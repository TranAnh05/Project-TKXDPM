package cgx.com.usecase.ManageUser.ViewUserProfile;

import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;

public class AdminViewUserDetailsUseCase extends AbstractViewUserProfileUseCase{

	public AdminViewUserDetailsUseCase(IAuthTokenValidator tokenValidator, IUserRepository userRepository,
			ViewUserProfileOutputBoundary outputBoundary) {
		super(tokenValidator, userRepository, outputBoundary);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getUserIdToView(AuthPrincipal principal, ViewUserProfileRequestData input) {
		// 1. Phân quyền (Authorization)
        if (principal.role != UserRole.ADMIN) {
            throw new SecurityException("Không có quyền truy cập.");
        }

        // 2. Validate Input (Xác thực đầu vào)
        if (input.targetUserId == null || input.targetUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("Phải cung cấp ID người dùng mục tiêu (targetUserId).");
        }

        // 3. Trả về ID mục tiêu
        return input.targetUserId;
	}

}
