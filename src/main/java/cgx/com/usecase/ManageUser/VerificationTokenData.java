package cgx.com.usecase.ManageUser;

import java.time.Instant;

public class VerificationTokenData {
	public final String token;      // Chuỗi ngẫu nhiên (ví dụ: UUID)
    public final String userId;     // Token này của ai?
    public final Instant expiryDate; // Khi nào hết hạn?

    public VerificationTokenData(String token, String userId, Instant expiryDate) {
        this.token = token;
        this.userId = userId;
        this.expiryDate = expiryDate;
    }
}
