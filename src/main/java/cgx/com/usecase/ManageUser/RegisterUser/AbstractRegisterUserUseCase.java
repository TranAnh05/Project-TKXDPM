package cgx.com.usecase.ManageUser.RegisterUser;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import cgx.com.Entities.User;
import cgx.com.usecase.Interface_Common.IEmailService;
import cgx.com.usecase.Interface_Common.IPasswordHasher;
import cgx.com.usecase.Interface_Common.ISecureTokenGenerator;
import cgx.com.usecase.Interface_Common.IVerificationTokenRepository;
import cgx.com.usecase.Interface_Common.VerificationTokenData;
import cgx.com.usecase.ManageUser.IUserIdGenerator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

/**
 * Lớp Use Case TRỪU TƯỢNG (Abstract) cho việc Đăng ký Người dùng.
 * Lớp này chứa tất cả logic nghiệp vụ chung, các bước thực thi
 */
public abstract class AbstractRegisterUserUseCase implements RegisterUserInputBoundary {

	protected IUserRepository userRepository;
    protected IPasswordHasher passwordHasher;
    protected IUserIdGenerator userIdGenerator;
    protected RegisterUserOutputBoundary outputBoundary;
    
    protected IEmailService emailService;
    protected ISecureTokenGenerator tokenGenerator;
    protected IVerificationTokenRepository verificationTokenRepository; 

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
            User.validateEmail(input.email);
            User.validateName(input.firstName, input.lastName);
            
            // Validate riêng
            validateRegistrationTypeSpecific(input);
            
            if (userRepository.findByEmail(input.email) != null) {
                throw new IllegalArgumentException("Email này đã tồn tại.");
            }

            User userEntity = createEntity(input);

            // Chuyển entity sang dto
            UserData dataToSave = mapEntityToData(userEntity);

            UserData savedData = userRepository.save(dataToSave);
            
            // Tạo token xác thực để gửi email
            String verificationToken = tokenGenerator.generate();
            Instant expiryDate = Instant.now().plus(24, ChronoUnit.HOURS);
            
            VerificationTokenData tokenData = new VerificationTokenData(
            		verificationToken, 
            		savedData.userId, 
                    expiryDate
                );
            
            verificationTokenRepository.save(tokenData);
            
            // Logic riêng của class con -> Gửi email xác thực
            sendActivationEmail(savedData, verificationToken);
            
            output.success = true;
            output.message = "Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.";
            output.createdUserId = savedData.userId;
            output.email = savedData.email;

        } catch (IllegalArgumentException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            // 9. Bắt lỗi hệ thống
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống không xác định. Vui lòng thử lại sau.";
        }

        outputBoundary.present(output);
    }

    protected abstract void sendActivationEmail(UserData savedData, String verificationToken);

    protected abstract void validateRegistrationTypeSpecific(RegisterUserRequestData input) throws IllegalArgumentException;
    
    protected abstract User createEntity(RegisterUserRequestData input);

    protected abstract UserData mapEntityToData(User user);
}
