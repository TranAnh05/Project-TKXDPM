package cgx.com.Entities;

import java.time.Instant;

public class VerificationToken {
	private String token;      // Chuỗi ngẫu nhiên (ví dụ: UUID)
	private String userId;     // Token này của ai?
	private Instant expiryDate; // Khi nào hết hạn?

    public VerificationToken(String token, String userId, Instant expiryDate) {
        this.token = token;
        this.userId = userId;
        this.expiryDate = expiryDate;
    }

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Instant getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Instant expiryDate) {
		this.expiryDate = expiryDate;
	}
    
	public static void validateToken(String token) {
		if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã xác thực không được để trống.");
        }
	} 
}
