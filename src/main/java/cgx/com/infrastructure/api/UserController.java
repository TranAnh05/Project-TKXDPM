package cgx.com.infrastructure.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cgx.com.adapters.ManageUser.ChangePassword.ChangePasswordPresenter;
import cgx.com.adapters.ManageUser.UpdateUserProfile.UpdateUserProfilePresenter;
import cgx.com.adapters.ManageUser.UpdateUserProfile.UpdateUserProfileViewModel;
import cgx.com.adapters.ManageUser.ViewUserProfile.ViewUserProfilePresenter;
import cgx.com.adapters.ManageUser.ViewUserProfile.ViewUserProfileViewModel;
import cgx.com.usecase.ManageUser.ChangePassword.ChangePasswordInputBoundary;
import cgx.com.usecase.ManageUser.UpdateUserProfile.UpdateUserProfileInputBoundary;
import cgx.com.usecase.ManageUser.UpdateUserProfile.UpdateUserProfileRequestData;
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileInputBoundary;
import cgx.com.usecase.ManageUser.ViewUserProfile.ViewUserProfileRequestData;

@RestController
@RequestMapping("/api/users") // API bắt đầu bằng /api/users
public class UserController {

    private final ViewUserProfileInputBoundary viewProfileUseCase;
    private final ViewUserProfilePresenter viewProfilePresenter;
    private final UpdateUserProfileInputBoundary updateProfileUseCase;
    private final UpdateUserProfilePresenter updateProfilePresenter;
    private final ChangePasswordInputBoundary changePasswordUseCase;
    private final ChangePasswordPresenter changePasswordPresenter;

    public UserController(ViewUserProfileInputBoundary viewProfileUseCase,
                          ViewUserProfilePresenter viewProfilePresenter,
                          UpdateUserProfileInputBoundary updateProfileUseCase, // <--- Kiểm tra Constructor
                          UpdateUserProfilePresenter updateProfilePresenter,
                          ChangePasswordInputBoundary changePasswordUseCase,
                          ChangePasswordPresenter changePasswordPresenter) {
        this.viewProfileUseCase = viewProfileUseCase;
        this.viewProfilePresenter = viewProfilePresenter;
        this.updateProfileUseCase = updateProfileUseCase;
        this.updateProfilePresenter = updateProfilePresenter;
        this.changePasswordUseCase = changePasswordUseCase;
        this.changePasswordPresenter = changePasswordPresenter;
    }

    // GET /api/users/me
    @GetMapping("/me")
    public ResponseEntity<ViewUserProfileViewModel> viewMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // 1. Lấy token từ Header (InputData của chúng ta nhận chuỗi "Bearer ...")
        // Nếu không có header, use case sẽ validate và báo lỗi
        String token = authHeader != null ? authHeader : "";

        // 2. Tạo Input Data
        ViewUserProfileRequestData inputData = new ViewUserProfileRequestData(token);

        // 3. Gọi Use Case (Nó sẽ tự validate token, lấy userId từ token, và tìm user)
        viewProfileUseCase.execute(inputData);

        // 4. Lấy kết quả
        ViewUserProfileViewModel viewModel = viewProfilePresenter.getModel();

        if ("true".equals(viewModel.success)) {
            return ResponseEntity.ok(viewModel);
        } else {
            return ResponseEntity.status(401).body(viewModel);
        }
    }
    
    @PutMapping("/me") // <--- Đây là cái Postman đang tìm
    public ResponseEntity<UpdateUserProfileViewModel> updateMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody UpdateProfileRequestJson request
    ) {
        String token = authHeader != null ? authHeader : "";
        UpdateUserProfileRequestData input = new UpdateUserProfileRequestData(
            token, request.firstName, request.lastName, request.phoneNumber
        );
        
        updateProfileUseCase.execute(input);
        UpdateUserProfileViewModel viewModel = updateProfilePresenter.getModel();
        
        return "true".equals(viewModel.success) ? ResponseEntity.ok(viewModel) : ResponseEntity.badRequest().body(viewModel);
    }
    
    public static class UpdateProfileRequestJson {
        public String firstName;
        public String lastName;
        public String phoneNumber;
    }
}
