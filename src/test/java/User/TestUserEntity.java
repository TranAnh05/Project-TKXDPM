package User;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import Entities.User;
import Entities.UserRole;

public class TestUserEntity {
	// User failed
	@Test
    public void testValidateEmail_Fail_Invalid() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            User.validateEmail("email.com"); // Thiếu @
        });
        assertEquals("Định dạng email không hợp lệ.", e.getMessage());
    }
    
    @Test
    public void testValidatePassword_Fail_TooShort() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            User.validatePassword("123"); // Quá ngắn
        });
        assertEquals("Mật khẩu phải có ít nhất 6 ký tự.", e.getMessage());
    }
    
    @Test
    public void testValidateFullName_Fail() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            User.validateFullName(""); 
        });
        assertEquals("Tên không được để trống.", e.getMessage());
    }
    
    @Test
    public void testValidateAddress_Fail() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            User.validateAddress(""); 
        });
        assertEquals("Địa chỉ không được để trống.", e.getMessage());
    }
    
    @Test
    public void testValidateRole_Fail_InvalidString() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            User.validateRole("MODERATOR"); // String không hợp lệ
        });
        assertEquals("Vai trò không hợp lệ.", e.getMessage());
    }
    
    
    // User passed
    @Test
    public void testValidate_Success() {
        assertDoesNotThrow(() -> {
            User.validateEmail("test@example.com");
            User.validatePassword("123456");
            User.validateFullName("Tran Anh");
            User.validateAddress("TP.HCM");
            User.validateRole("Admin");
            User.validateRole("ADMIN");
        });
    }
    
}
