package cgx.com.usecase.ManageUser.ViewUserProfile;

import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public class ViewUserProfileUseCase implements ViewUserProfileInputBoundary{
	protected final IAuthTokenValidator tokenValidator;
    protected final IUserRepository userRepository;
    protected final ViewUserProfileOutputBoundary outputBoundary;
    
    public ViewUserProfileUseCase(IAuthTokenValidator tokenValidator,
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
            AuthPrincipal principal = tokenValidator.validate(input.authToken);
            
            UserData userData = userRepository.findByUserId(principal.userId);
            
            output.success = true;
            output.message = "Lấy thông tin thành công.";
            output.userId = userData.userId;
            output.email = userData.email;
            output.firstName = userData.firstName;
            output.lastName = userData.lastName;
            output.phoneNumber = userData.phoneNumber;

        } catch (SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống không xác định.";
        }

        outputBoundary.present(output);
    }
}
