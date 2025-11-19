package cgx.com.infrastructure.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cgx.com.adapters.ManageUser.AuthencicateUser.AuthenticateUserPresenter;
import cgx.com.adapters.ManageUser.AuthencicateUser.AuthenticateUserViewModel;
import cgx.com.adapters.ManageUser.RegisterUser.RegisterUserPresenter;
import cgx.com.adapters.ManageUser.RegisterUser.RegisterUserViewModel;
import cgx.com.adapters.ManageUser.RequestPasswordReset.RequestPasswordResetPresenter;
import cgx.com.adapters.ManageUser.VerifyPasswordReset.VerifyPasswordResetPresenter;
import cgx.com.usecase.ManageUser.AuthenticateUser.AuthenticateUserInputBoundary;
import cgx.com.usecase.ManageUser.AuthenticateUser.AuthenticateUserRequestData;
import cgx.com.usecase.ManageUser.RegisterUser.RegisterUserInputBoundary;
import cgx.com.usecase.ManageUser.RegisterUser.RegisterUserRequestData;
import cgx.com.usecase.ManageUser.RequestPasswordReset.RequestPasswordResetInputBoundary;
import cgx.com.usecase.ManageUser.VerifyPasswordReset.VerifyPasswordResetInputBoundary;

/**
 * MENTOR NOTE:
 * Đây là REST Controller.
 * - @RestController: Biến class này thành nơi xử lý API.
 * - @RequestMapping("/api/auth"): Tất cả API trong này bắt đầu bằng /api/auth
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final RegisterUserInputBoundary registerUseCase;
    private final RegisterUserPresenter registerPresenter;
    
    // Inject thêm cho Login
    private final AuthenticateUserInputBoundary loginUseCase;
    private final AuthenticateUserPresenter loginPresenter;
    private final RequestPasswordResetInputBoundary requestResetUseCase;
    private final RequestPasswordResetPresenter requestResetPresenter;
    private final VerifyPasswordResetInputBoundary verifyResetUseCase;
    private final VerifyPasswordResetPresenter verifyResetPresenter;

    public AuthController(RegisterUserInputBoundary registerUseCase,
                          RegisterUserPresenter registerPresenter,
                          AuthenticateUserInputBoundary loginUseCase,
                          AuthenticateUserPresenter loginPresenter,
                          RequestPasswordResetInputBoundary requestResetUseCase,
                          RequestPasswordResetPresenter requestResetPresenter,
                          VerifyPasswordResetInputBoundary verifyResetUseCase,
                          VerifyPasswordResetPresenter verifyResetPresenter) {
        this.registerUseCase = registerUseCase;
        this.registerPresenter = registerPresenter;
        this.loginUseCase = loginUseCase;
        this.loginPresenter = loginPresenter;
        this.requestResetUseCase = requestResetUseCase;
        this.requestResetPresenter = requestResetPresenter;
        this.verifyResetUseCase = verifyResetUseCase;
        this.verifyResetPresenter = verifyResetPresenter;
    }
    
    @PostMapping("/register")
    public ResponseEntity<RegisterUserViewModel> register(@RequestBody RegisterRequestJson request) {
        // 1. Chuyển đổi JSON Request thành Input Data
        RegisterUserRequestData inputData = new RegisterUserRequestData(
            request.email, request.password, request.firstName, request.lastName
        );

        // 2. Thực thi Use Case
        registerUseCase.execute(inputData);

        // 3. Lấy kết quả từ Presenter (Lấy ViewModel thuần từ Presenter)
        RegisterUserViewModel viewModel = registerPresenter.getModel(); 

        // 4. Trả về JSON
        if ("true".equals(viewModel.success)) {
            return ResponseEntity.ok(viewModel);
        } else {
            return ResponseEntity.badRequest().body(viewModel);
        }
    }
    
    // --- API MỚI: LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<AuthenticateUserViewModel> login(@RequestBody LoginRequestJson request) {
        // 1. Tạo Input Data
        AuthenticateUserRequestData inputData = new AuthenticateUserRequestData(
            request.email, request.password
        );

        // 2. Gọi Use Case
        loginUseCase.execute(inputData);

        // 3. Lấy kết quả
        AuthenticateUserViewModel viewModel = loginPresenter.getModel();

        // 4. Trả về JSON
        if ("true".equals(viewModel.success)) {
            return ResponseEntity.ok(viewModel);
        } else {
            return ResponseEntity.status(401).body(viewModel); // 401 Unauthorized
        }
    }

    // DTO nội bộ
    public static class RegisterRequestJson {
        public String email;
        public String password;
        public String firstName;
        public String lastName;
    }

    public static class LoginRequestJson {
        public String email;
        public String password;
    }
}
