package usecase.ManageUser.RegisterUser;

import Entities.User;
import usecase.ManageUser.IPasswordHasher;
import usecase.ManageUser.IUserIdGenerator;
import usecase.ManageUser.IUserRepository;
import usecase.ManageUser.UserData;

/**
 * Lớp Use Case CỤ THỂ (Concrete) đầu tiên: Đăng ký bằng Email.
 * Lớp này "cắm" vào lớp AbstractRegisterUserUseCase,
 * cung cấp các logic cụ thể cho kiểu đăng ký này.
 */
public class RegisterByEmailUseCase extends AbstractRegisterUserUseCase {

    // Constructor nhận dependencies và đẩy lên lớp Cha (super)
    public RegisterByEmailUseCase(IUserRepository userRepository,
                                  IPasswordHasher passwordHasher,
                                  IUserIdGenerator userIdGenerator,
                                  RegisterUserOutputBoundary outputBoundary) {
        super(userRepository, passwordHasher, userIdGenerator, outputBoundary);
    }

    @Override
    protected void validateRegistrationTypeSpecific(RegisterUserRequestData input) throws IllegalArgumentException {
        // Logic validation "riêng" của kiểu đăng ký này:
        // Phải validate mật khẩu.
        // (RegisterByGoogle sẽ không cần bước này)
        User.validatePassword(input.password);
    }

    @Override
    protected User createEntity(RegisterUserRequestData input) {
        // Logic tạo Entity "riêng" của kiểu đăng ký này:
        
        // 1. Tạo ID mới
        String userId = this.userIdGenerator.generate();
        
        // 2. Băm mật khẩu (dùng service đã được inject)
        String hashedPassword = this.passwordHasher.hash(input.password);

        // 3. Gọi Factory của Entity (Layer 4)
        return User.createNewCustomer(
            userId,
            input.email,
            hashedPassword,
            input.firstName,
            input.lastName
        );
    }

	@Override
	protected UserData mapEntityToData(User user) {
		// Logic mapping "riêng":
        // Trong trường hợp này, nó chỉ là map 1:1 đơn giản.
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