package cgx.com.usecase.ManageUser.RegisterUser;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import cgx.com.Entities.User;
import cgx.com.usecase.ManageUser.IEmailService;
import cgx.com.usecase.ManageUser.IPasswordHasher;
import cgx.com.usecase.ManageUser.ISecureTokenGenerator;
import cgx.com.usecase.ManageUser.IUserIdGenerator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.IVerificationTokenRepository;
import cgx.com.usecase.ManageUser.UserData;
import cgx.com.usecase.ManageUser.VerificationTokenData;

/**
 * Lớp Use Case TRỪU TƯỢNG (Abstract) cho việc Đăng ký Người dùng.
 * Lớp này chứa tất cả logic nghiệp vụ chung, các bước thực thi
 */
public abstract class AbstractRegisterUserUseCase implements RegisterUserInputBoundary {

	protected IUserRepository userRepository;
    protected IPasswordHasher passwordHasher;
    protected IUserIdGenerator userIdGenerator;
    protected RegisterUserOutputBoundary outputBoundary;
    
    // [MỚI] Thêm dependency gửi mail và tạo token
    protected IEmailService emailService;
    protected ISecureTokenGenerator tokenGenerator;
    protected IVerificationTokenRepository verificationTokenRepository; // Repository riêng

    public AbstractRegisterUserUseCase(IUserRepository userRepository,
                                       IPasswordHasher passwordHasher,
                                       IUserIdGenerator userIdGenerator,
                                       IEmailService emailService,           // Inject thêm
                                       ISecureTokenGenerator tokenGenerator,
                                       IVerificationTokenRepository verificationTokenRepository,// Inject thêm
                                       RegisterUserOutputBoundary outputBoundary) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.userIdGenerator = userIdGenerator;
        this.emailService = emailService;
        this.tokenGenerator = tokenGenerator;
        this.verificationTokenRepository = verificationTokenRepository;
        this.outputBoundary = outputBoundary;
    }
    
    @Override
    public final void execute(RegisterUserRequestData input) {
        RegisterUserResponseData output = new RegisterUserResponseData();
        
        try {
            // 1. Validate chung (Tầng 4 - Entity)
            User.validateEmail(input.email);
            User.validateName(input.firstName, input.lastName);
            
            // 2. Validate riêng (Tầng 4 - ConCRETE)
            // (Hàm này là 'abstract' - lớp Con sẽ tự định nghĩa)
            validateRegistrationTypeSpecific(input);

            // 3. Kiểm tra nghiệp vụ (Tầng 3 - Chung)
            if (userRepository.findByEmail(input.email) != null) {
                throw new IllegalArgumentException("Email này đã tồn tại.");
            }

            // 4. TẠO ENTITY (Tầng 4 - ConCRETE)
            // (Hàm này là 'abstract' - lớp Con sẽ tạo Entity)
            User userEntity = createEntity(input);

            // 5. CHUYỂN (MAP) (Tầng 4 -> T3 DTO - ConCRETE)
            // (Hàm này là 'abstract' - lớp Con tự "làm phẳng" (flatten))
            UserData dataToSave = mapEntityToData(userEntity);

            // 6. LƯU VÀO CSDL (Tầng 3 - Chung)
            UserData savedData = userRepository.save(dataToSave);
            
            String verificationToken = tokenGenerator.generate();
            Instant expiryDate = Instant.now().plus(24, ChronoUnit.HOURS);
            
            VerificationTokenData tokenData = new VerificationTokenData(
            		verificationToken, // Nếu muốn bảo mật hơn, có thể hash chuỗi này trước khi lưu
            		savedData.userId, 
                    expiryDate
                );
            
            verificationTokenRepository.save(tokenData);
            
            sendActivationEmail(savedData, verificationToken);
            
            // 7. Báo cáo thành công (Chung)
            output.success = true;
            output.message = "Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.";
            output.createdUserId = savedData.userId;
            output.email = savedData.email;

        } catch (IllegalArgumentException e) {
            // 8. BẮT LỖI VALIDATION (T4) HOẶC LỖI NGHIỆP VỤ (T3)
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            // 9. Bắt lỗi hệ thống
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống không xác định. Vui lòng thử lại sau.";
            // (Nên log lỗi `e` này ra)
        }

        // 10. Trình bày kết quả (Chung)
        outputBoundary.present(output);
    }

    protected abstract void sendActivationEmail(UserData savedData, String verificationToken);

	/**
     * Lớp Con (Concrete) phải implement hàm này để validate
     * các trường riêng của kiểu đăng ký đó.
     */
    protected abstract void validateRegistrationTypeSpecific(RegisterUserRequestData input) throws IllegalArgumentException;
    
    /**
     * Lớp Con (Concrete) phải implement hàm này để tạo
     * Entity User theo đúng kiểu đăng ký.
     */
    protected abstract User createEntity(RegisterUserRequestData input);

    /**
     * Lớp Con (Concrete) phải implement hàm này để map
     * từ Entity (Layer 4) sang UserData DTO (Layer 3).
     */
    protected abstract UserData mapEntityToData(User user);

}
