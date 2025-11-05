package Order;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import domain.entities.Order;

public class TestEntity {
	// User failed
	@Test
    public void testValidateStatus_Fail() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Order.validateStatus("");
        });
        assertEquals("Trạng thái không được rỗng.", e.getMessage());
    }
	
	@Test
    public void testValidateStatus_Fail_02() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Order.validateStatus("abc");
        });
        assertEquals("Trạng thái không hợp lệ.", e.getMessage());
    }
	
	@Test
    public void testValidate_Success() {
        assertDoesNotThrow(() -> {
        	Order.validateStatus("Pending");
        });
    }
}
