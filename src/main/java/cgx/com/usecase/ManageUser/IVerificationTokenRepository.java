package cgx.com.usecase.ManageUser;

public interface IVerificationTokenRepository {
	void save(VerificationTokenData tokenData);
	VerificationTokenData findByToken(String token);
	void deleteByToken(String token);
}
