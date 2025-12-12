package cgx.com.usecase.ManageUser.AdminCreateNewUser;

import java.time.Instant;

import cgx.com.Entities.AccountStatus;
import cgx.com.Entities.User;
import cgx.com.Entities.UserRole;
import cgx.com.usecase.Interface_Common.AuthPrincipal;
import cgx.com.usecase.Interface_Common.IAuthTokenValidator;
import cgx.com.usecase.Interface_Common.IEmailService;
import cgx.com.usecase.Interface_Common.IPasswordHasher;
import cgx.com.usecase.ManageUser.IUserIdGenerator;
import cgx.com.usecase.ManageUser.IUserRepository;
import cgx.com.usecase.ManageUser.UserData;

public class AdminCreateUserUseCase implements AdminCreateUserInputBoundary{
	private final IAuthTokenValidator tokenValidator;
	private final IUserRepository userRepository;
	private final IPasswordHasher passwordHasher;
	private final IUserIdGenerator userIdGenerator;
	private final IEmailService emailService;
	private final AdminCreateUserOutputBoundary outputBoundary;
    
	
    public AdminCreateUserUseCase(IAuthTokenValidator tokenValidator,
            IUserRepository userRepository,
            IPasswordHasher passwordHasher,
            IUserIdGenerator userIdGenerator,
            IEmailService emailService,
            AdminCreateUserOutputBoundary outputBoundary) {
		this.tokenValidator = tokenValidator;
		this.userRepository = userRepository;
		this.passwordHasher = passwordHasher;
		this.userIdGenerator = userIdGenerator;
		this.emailService = emailService;
		this.outputBoundary = outputBoundary;
	}
    
	@Override
	public void execute(AdminCreateUserRequestData input) {
		AdminCreateUserResponseData output = new AdminCreateUserResponseData();

        try {
            // Xác thực Token
            AuthPrincipal adminPrincipal = tokenValidator.validate(input.authToken);
            
            // Kiểm tra dữ liệu đầu vào
            User.validateIsAdmin(adminPrincipal.role);
            
            User.validateEmail(input.email);
            User.validateName(input.firstName, input.lastName);
            User.validatePhoneNumber(input.phoneNumber);
            User.validatePassword(input.password);
            UserRole newUserRole = User.validateRole(input.role); 
            AccountStatus newUserStatus = User.validateStatus(input.status); 

            if (userRepository.findByEmail(input.email) != null) {
                throw new IllegalArgumentException("Email này đã tồn tại.");
            }

            String newUserId = userIdGenerator.generate();
            String newHashedPassword = passwordHasher.hash(input.password);
            
            User userEntity = new User(
                newUserId,
                input.email,
                newHashedPassword,
                input.firstName,
                input.lastName,
                input.phoneNumber,
                newUserRole,    
                newUserStatus,  
                Instant.now(),
                Instant.now()
            );

            UserData dataToSave = mapEntityToData(userEntity);
            UserData savedData = userRepository.save(dataToSave);
            
            emailService.sendAccountCreatedEmail(
                    savedData.email, 
                    savedData.firstName, 	
                    input.password 
                );
            
            output.success = true;
            output.message = "Tạo tài khoản thành công!";
            output.createdUserId = savedData.userId;
            output.email = savedData.email;
            output.fullName = savedData.firstName + " " + savedData.lastName;
            output.role = savedData.role;
            output.status = savedData.status;

        } catch (IllegalArgumentException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (SecurityException e) {
            output.success = false;
            output.message = e.getMessage();
        } catch (Exception e) {
            // Bắt lỗi hệ thống
            output.success = false;
            output.message = "Đã xảy ra lỗi hệ thống không xác định.";
        }

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
