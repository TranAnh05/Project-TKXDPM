package Category;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import domain.entities.Category;

public class TestCategoryEntity {
	@Test
	public void testIsValidName_Fail_Empty() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Category.isValidName(""); // Tên trống
        });
        assertEquals("Tên loại sản phẩm không được để trống.", e.getMessage());
    }
	
	@Test
    public void testIsValidName_Success() {
        assertDoesNotThrow(() -> { // Khẳng định KHÔNG ném lỗi
            Category.isValidName("Laptop");
        });
    }
}
