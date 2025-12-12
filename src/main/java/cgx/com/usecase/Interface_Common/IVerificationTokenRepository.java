package cgx.com.usecase.Interface_Common;

public interface IVerificationTokenRepository {
	void save(VerificationTokenData tokenData);
	VerificationTokenData findByToken(String token);
	void deleteByToken(String token);
}
