package infrastructure.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import adapters.ManageUser.RegisterUser.RegisterUserViewModel;
import usecase.ManageUser.RegisterUser.RegisterUserInputBoundary;
import usecase.ManageUser.RegisterUser.RegisterUserRequestData;

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
    private final RegisterUserViewModel registerViewModel; // Inject ViewModel để lấy kết quả

    public AuthController(RegisterUserInputBoundary registerUseCase,
                          RegisterUserViewModel registerViewModel) {
        this.registerUseCase = registerUseCase;
        this.registerViewModel = registerViewModel;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserViewModel> register(@RequestBody RegisterRequestJson request) {
        // 1. Chuyển đổi JSON Request thành Input Data
        RegisterUserRequestData inputData = new RegisterUserRequestData(
            request.email, request.password, request.firstName, request.lastName
        );

        // 2. Thực thi Use Case
        // (Use Case sẽ gọi Presenter, Presenter sẽ cập nhật ViewModel)
        registerUseCase.execute(inputData);

        // 3. Lấy kết quả từ ViewModel (lúc này đã có dữ liệu)
        if ("true".equals(registerViewModel.success)) {
            return ResponseEntity.ok(registerViewModel);
        } else {
            return ResponseEntity.badRequest().body(registerViewModel);
        }
    }

    // Class DTO nội bộ để hứng JSON từ Body
    public static class RegisterRequestJson {
        public String email;
        public String password;
        public String firstName;
        public String lastName;
    }
}
