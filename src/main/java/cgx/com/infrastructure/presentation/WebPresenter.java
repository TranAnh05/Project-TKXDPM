package cgx.com.infrastructure.presentation;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import cgx.com.usecase.ManageUser.AuthenticateUser.AuthenticateUserOutputBoundary;
import cgx.com.usecase.ManageUser.AuthenticateUser.AuthenticateUserResponseData;
import cgx.com.usecase.ManageUser.RegisterUser.RegisterUserOutputBoundary;
import cgx.com.usecase.ManageUser.RegisterUser.RegisterUserResponseData;
import cgx.com.usecase.ManageUser.VerifyEmail.VerifyEmailOutputBoundary;
import cgx.com.usecase.ManageUser.VerifyEmail.VerifyEmailResponseData;

@Component
@RequestScope 
public class WebPresenter implements RegisterUserOutputBoundary, AuthenticateUserOutputBoundary, VerifyEmailOutputBoundary {

    private Object response; // Biến chung để chứa kết quả

    // --- Xử lý Đăng Ký ---
    @Override
    public void present(RegisterUserResponseData responseData) {
        this.response = responseData;
    }
    
    @Override
    public void present(VerifyEmailResponseData responseData) {
        this.response = responseData;
    }
    
    // --- Xử lý Đăng Nhập ---
    @Override
    public void present(AuthenticateUserResponseData responseData) {
        this.response = responseData;
    }

    // Hàm để Controller lấy kết quả ra
    public Object getResponse() {
        return response;
    }
}