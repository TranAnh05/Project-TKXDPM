package cgx.com.infrastructure.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cgx.com.adapters.ManageUser.AdminCreatedUser.AdminCreateUserPresenter;
import cgx.com.adapters.ManageUser.AdminCreatedUser.AdminCreateUserViewModel;
import cgx.com.adapters.ManageUser.AdminUpdateUser.AdminUpdateUserPresenter;
import cgx.com.adapters.ManageUser.AdminUpdateUser.AdminUpdateUserViewModel;
import cgx.com.adapters.ManageUser.AdminViewUserDetails.AdminViewUserDetailsPresenter;
import cgx.com.adapters.ManageUser.AdminViewUserDetails.AdminViewUserDetailsViewModel;
import cgx.com.adapters.ManageUser.DeleteUser.DeleteUserPresenter;
import cgx.com.adapters.ManageUser.DeleteUser.DeleteUserViewModel;
import cgx.com.adapters.ManageUser.SearchUsers.SearchUsersPresenter;
import cgx.com.adapters.ManageUser.SearchUsers.SearchUsersViewModel;
import cgx.com.usecase.ManageUser.AdminCreateNewUser.AdminCreateUserInputBoundary;
import cgx.com.usecase.ManageUser.AdminCreateNewUser.AdminCreateUserRequestData;
import cgx.com.usecase.ManageUser.AdminUpdateUser.AdminUpdateUserInputBoundary;
import cgx.com.usecase.ManageUser.AdminUpdateUser.AdminUpdateUserRequestData;
import cgx.com.usecase.ManageUser.AdminViewUserDetails.AdminViewUserDetailsInputBoundary;
import cgx.com.usecase.ManageUser.AdminViewUserDetails.AdminViewUserDetailsRequestData;
import cgx.com.usecase.ManageUser.DeleteUser.DeleteUserInputBoundary;
import cgx.com.usecase.ManageUser.DeleteUser.DeleteUserRequestData;
import cgx.com.usecase.ManageUser.SearchUsers.SearchUsersInputBoundary;
import cgx.com.usecase.ManageUser.SearchUsers.SearchUsersRequestData;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final SearchUsersInputBoundary searchUsersUseCase;
    private final SearchUsersPresenter searchUsersPresenter;
    
    private final AdminCreateUserInputBoundary createUserUseCase;
    private final AdminCreateUserPresenter createUserPresenter;
    
    private final AdminViewUserDetailsInputBoundary viewUserDetailUseCase;
    private final AdminViewUserDetailsPresenter viewUserDetailPresenter;
    
    private final AdminUpdateUserInputBoundary updateUserUseCase;
    private final AdminUpdateUserPresenter updateUserPresenter;
    
    private final DeleteUserInputBoundary deleteUserUseCase;
    private final DeleteUserPresenter deleteUserPresenter;

    public AdminUserController(
            SearchUsersInputBoundary searchUsersUseCase, SearchUsersPresenter searchUsersPresenter,
            AdminCreateUserInputBoundary createUserUseCase, AdminCreateUserPresenter createUserPresenter,
            AdminViewUserDetailsInputBoundary viewUserDetailUseCase, AdminViewUserDetailsPresenter viewUserDetailPresenter,
            AdminUpdateUserInputBoundary updateUserUseCase, AdminUpdateUserPresenter updateUserPresenter,
            DeleteUserInputBoundary deleteUserUseCase, DeleteUserPresenter deleteUserPresenter) {
        this.searchUsersUseCase = searchUsersUseCase;
        this.searchUsersPresenter = searchUsersPresenter;
        this.createUserUseCase = createUserUseCase;
        this.createUserPresenter = createUserPresenter;
        this.viewUserDetailUseCase = viewUserDetailUseCase;
        this.viewUserDetailPresenter = viewUserDetailPresenter;
        this.updateUserUseCase = updateUserUseCase;
        this.updateUserPresenter = updateUserPresenter;
        this.deleteUserUseCase = deleteUserUseCase;
        this.deleteUserPresenter = deleteUserPresenter;
    }

    // 1. Tìm kiếm User (GET /api/admin/users?keyword=abc&page=1)
    @GetMapping
    public ResponseEntity<SearchUsersViewModel> searchUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        String token = authHeader != null ? authHeader : "";
        searchUsersUseCase.execute(new SearchUsersRequestData(token, keyword, page, size));
        return ResponseEntity.ok(searchUsersPresenter.getModel());
    }

    // 2. Tạo User Mới (POST /api/admin/users)
    @PostMapping
    public ResponseEntity<AdminCreateUserViewModel> createUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateUserRequestJson request
    ) {
        String token = authHeader != null ? authHeader : "";
        AdminCreateUserRequestData input = new AdminCreateUserRequestData(
            token, request.email, request.password, request.firstName, request.lastName, request.role, request.status
        );
        createUserUseCase.execute(input);
        return ResponseEntity.ok(createUserPresenter.getModel());
    }

    // 3. Xem Chi tiết User (GET /api/admin/users/{id})
    @GetMapping("/{id}")
    public ResponseEntity<AdminViewUserDetailsViewModel> getUserDetail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        String token = authHeader != null ? authHeader : "";
        viewUserDetailUseCase.execute(new AdminViewUserDetailsRequestData(token, id));
        return ResponseEntity.ok(viewUserDetailPresenter.getModel());
    }

    // 4. Cập nhật User (PUT /api/admin/users/{id})
    @PutMapping("/{id}")
    public ResponseEntity<AdminUpdateUserViewModel> updateUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestBody UpdateUserRequestJson request
    ) {
        String token = authHeader != null ? authHeader : "";
        AdminUpdateUserRequestData input = new AdminUpdateUserRequestData(
            token, id, request.email, request.firstName, request.lastName, 
            request.phoneNumber, request.role, request.status
        );
        updateUserUseCase.execute(input);
        return ResponseEntity.ok(updateUserPresenter.getModel());
    }

    // 5. Xóa User (DELETE /api/admin/users/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteUserViewModel> deleteUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        String token = authHeader != null ? authHeader : "";
        deleteUserUseCase.execute(new DeleteUserRequestData(token, id));
        return ResponseEntity.ok(deleteUserPresenter.getModel());
    }

    // DTOs
    public static class CreateUserRequestJson {
        public String email;
        public String password;
        public String firstName;
        public String lastName;
        public String role;
        public String status;
    }
    public static class UpdateUserRequestJson {
        public String email;
        public String firstName;
        public String lastName;
        public String phoneNumber;
        public String role;
        public String status;
    }
}
