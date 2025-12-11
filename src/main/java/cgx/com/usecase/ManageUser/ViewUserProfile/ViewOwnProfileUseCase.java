package cgx.com.usecase.ManageUser.ViewUserProfile;

import cgx.com.usecase.ManageUser.AuthPrincipal;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;

/**
 * Lớp Use Case CỤ THỂ (Concrete): Xem Hồ sơ CỦA CHÍNH MÌNH.
 * Cung cấp logic "riêng" cho lớp Cha.
 */
public class ViewOwnProfileUseCase extends AbstractViewUserProfileUseCase{

	public ViewOwnProfileUseCase(IAuthTokenValidator tokenValidator, IUserRepository userRepository,
			ViewUserProfileOutputBoundary outputBoundary) {
		super(tokenValidator, userRepository, outputBoundary);
		// TODO Auto-generated constructor stub
	}

	 /**
     * Logic "riêng" cho nghiệp vụ này:
     * "Tôi muốn xem hồ sơ của CHÍNH MÌNH."
     * -> Luôn trả về ID của người dùng từ token đã xác thực.
     * Điều này ngăn chặn một user xem hồ sơ của user khác.
     */
    @Override
    protected String getUserIdToView(AuthPrincipal principal, ViewUserProfileRequestData input) {
        return principal.userId;
    }
}
