package cgx.com.usecase.Interface_Common;

/**
 * Interface cho dịch vụ tạo ID duy nhất cho
 * bản ghi PasswordResetToken (giống IUserIdGenerator).
 */
public interface IPasswordResetTokenIdGenerator {
	 String generate();
}
