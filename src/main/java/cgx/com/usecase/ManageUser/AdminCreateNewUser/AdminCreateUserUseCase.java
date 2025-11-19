package cgx.com.usecase.ManageUser.AdminCreateNewUser;

import java.time.Instant;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.User;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.ManageUser.IAuthTokenValidator;
import cgx.com.usecase.ManageUser.IPasswordHasher;
import cgx.com.usecase.ManageUser.IUserIdGenerator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.ViewUserProfile.AuthPrincipal;

public class AdminCreateUserUseCase implements AdminCreateUserInputBoundary{
	protected final IAuthTokenValidator tokenValidator;
    protected final IUserRepository userRepository;
    protected final IPasswordHasher passwordHasher;
    protected final IUserIdGenerator userIdGenerator;
    protected final AdminCreateUserOutputBoundary outputBoundary;
	
    public AdminCreateUserUseCase(IAuthTokenValidator tokenValidator,
            IUserRepository userRepository,
            IPasswordHasher passwordHasher,
            IUserIdGenerator userIdGenerator,
            AdminCreateUserOutputBoundary outputBoundary) {
		this.tokenValidator = tokenValidator;
		this.userRepository = userRepository;
		this.passwordHasher = passwordHasher;
		this.userIdGenerator = userIdGenerator;
		this.outputBoundary = outputBoundary;
	}
    
	@Override
	public void execute(AdminCreateUserRequestData input) {
		AdminCreateUserResponseData output = new AdminCreateUserResponseData();

        try {
            // 1. Kiểm tra input (Token)
            if (input.authToken == null || input.authToken.trim().isEmpty()) {
                throw new SecurityException("Auth Token không được để trống.");
            }

            // 2. Xác thực Token & Phân quyền (Authorization)
            AuthPrincipal adminPrincipal = tokenValidator.validate(input.authToken);
            if (adminPrincipal.role != UserRole.ADMIN) {
                throw new SecurityException("Không có quyền truy cập.");
            }

            // 3. Validate dữ liệu người dùng mới (dùng Entity)
            User.validateEmail(input.email);
            User.validateName(input.firstName, input.lastName);
            User.validatePassword(input.password);
            UserRole newUserRole = User.validateRole(input.role); // Chuyển String -> Enum
            AccountStatus newUserStatus = User.validateStatus(input.status); // Chuyển String -> Enum

            // 4. Kiểm tra nghiệp vụ (Tầng 3 - Chung)
            if (userRepository.findByEmail(input.email) != null) {
                throw new IllegalArgumentException("Email này đã tồn tại.");
            }

            // 5. Tạo Entity (Layer 4)
            String newUserId = userIdGenerator.generate();
            String newHashedPassword = passwordHasher.hash(input.password);
            
            // Dùng constructor của Entity để tạo User với Role/Status cụ thể
            User userEntity = new User(
                newUserId,
                input.email,
                newHashedPassword,
                input.firstName,
                input.lastName,
                null, // phoneNumber (có thể thêm sau)
                newUserRole,    // <-- Role do Admin chỉ định
                newUserStatus,  // <-- Status do Admin chỉ định
                Instant.now(),
                Instant.now()
            );

            // 6. Map Entity sang DTO (Layer 3)
            UserData dataToSave = mapEntityToData(userEntity);

            // 7. LƯU VÀO CSDL (Tầng 3 - Chung)
            UserData savedData = userRepository.save(dataToSave);
            
            // 8. Báo cáo thành công (Chung)
            output.success = true;
            output.message = "Tạo tài khoản thành công!";
            output.createdUserId = savedData.userId;
            output.email = savedData.email;
            output.fullName = savedData.firstName + " " + savedData.lastName;
            output.role = savedData.role;
            output.status = savedData.status;

        } catch (IllegalArgumentException e) {
            // 9. BẮT LỖI VALIDATION (T4) HOẶC LỖI NGHIỆP VỤ (T3)
            output.success = false;
            output.message = e.getMessage();
        } catch (SecurityException e) {
            // 10. BẮT LỖI BẢO MẬT (T3)
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            // 11. Bắt lỗi hệ thống
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống không xác định.";
        }

        // 12. Trình bày kết quả (Chung)
        outputBoundary.present(output);
	}
	
	private UserData mapEntityToData(User user) {
        return new UserData(
            user.getUserId(),
            user.getEmail(),
            user.getHashedPassword(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber(),
            user.getRole(),
            user.getStatus(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

}
