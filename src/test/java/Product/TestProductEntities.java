package Product;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import Entities.Keyboard;
import Entities.Laptop;
import Entities.Mouse;
import Entities.Product;

public class TestProductEntities {
	// FAILED
	
	// Product	
	@Test
    public void testProduct_ValidateCommon_Fail_NameEmpty() {
        // Test Validation (Chung)
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            // Tên trống
            Product.validateCommon("", "description", 1000, 10); 
        });
        assertEquals("Tên sản phẩm không được để trống.", e.getMessage());
    }

    @Test
    public void testProduct_ValidateCommon_Fail_BadPrice() {
        // Test Validation (Chung)
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Product.validateCommon("Laptop", "description", -100, 10); // Giá âm
        });
        assertEquals("Giá sản phẩm không được âm.", e.getMessage());
    }
    
    @Test
    public void testProduct_ValidateCommon_Fail_DesEmpty() {
        // Test Validation (Chung)
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Product.validateCommon("Laptop", "", -100, 10); // description trống
        });
        assertEquals("Mô tả sản phẩm không được để trống.", e.getMessage());
    }
    
    @Test
    public void testProduct_ValidateCommon_Fail_BadStock() {
        // Test Validation (Chung)
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Product.validateCommon("Laptop", "description", 100, -10); // stock âm
        });
        assertEquals("Số lượng tồn kho không được âm.", e.getMessage());
    }
    
    // Laptop
    @Test
    public void testLaptop_ValidateSpecific_Fail_BadRam() {
        // Test Validation (Riêng)
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Laptop.validateSpecific("i7", 0, "14 inch"); // Ram = 0
        });
        assertEquals("RAM phải là số dương.", e.getMessage());
    }
    
    @Test
    public void testLaptop_ValidateSpecific_Fail_BadCpu() {
        // Test Validation (Riêng)
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Laptop.validateSpecific("", 16, "14 inch"); // CPU trống
        });
        assertEquals("CPU không được để trống.", e.getMessage());
    }
    
    @Test
    public void testLaptop_ValidateSpecific_Fail_BadScreenSize() {
        // Test Validation (Riêng)
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Laptop.validateSpecific("i7", 16, ""); // screen trống
        });
        assertEquals("Kích thước màn hình không được để trống.", e.getMessage());
    }
    
    // Mouse
    @Test
    public void testMouse_ValidateSpecific_Fail_BadConnectionType() {
        // Test Validation (Riêng)
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Mouse.validateSpecific(1000, ""); // connection type rỗng 
        });
        assertEquals("Loại connectionType không được để trống.", e.getMessage());
    }
    
    @Test
    public void testMouse_ValidateSpecific_Fail_BadDpi() {
        // Test Validation (Riêng)
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Mouse.validateSpecific(-1, "Có dây"); // dpi âm
        });
        assertEquals("DPI phải là số dương.", e.getMessage());
    }
    
    // Keyboard
    @Test
    public void testKeyboard_ValidateSpecific_Fail_BadSwitchType() {
        // Test Validation (Riêng)
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Keyboard.validateSpecific("", "test"); // switchType rỗng
        });
        assertEquals("Loại switch không được để trống.", e.getMessage());
    }
    
    @Test
    public void testKeyboard_ValidateSpecific_Fail_BadLayout() {
        // Test Validation (Riêng)
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Keyboard.validateSpecific("test", ""); // layout rỗng
        });
        assertEquals("Loại layout không được để trống.", e.getMessage());
    }
    
    // SUCCESS
    // Product
    @Test
    public void testProduct_ValidateSuccess() {
        // Test Kịch bản Thành công
        assertDoesNotThrow(() -> {
            Product.validateCommon("Laptop", "des", 1000, 10);
            Laptop.validateSpecific("i7", 16, "test");
            Mouse.validateSpecific(100, "test");
            Keyboard.validateSpecific("test", "test");
        });
    }
    
    @Test
    public void testGetSpecificAttributes() {
        // 1. Arrange: Tạo 1 Laptop (T4)
        Product laptop = new Laptop(1, "Laptop", "desc", 100, 10, "img", 1, "i7", 16, "15in");
        
        // 2. Arrange: Tạo 1 Mouse (T4)
        Product mouse = new Mouse(2, "Mouse", "desc", 50, 20, "img", 2, "Wireless", 800);
        
        // 3. Act
        Map<String, String> laptopAttrs = laptop.getSpecificAttributes();
        Map<String, String> mouseAttrs = mouse.getSpecificAttributes();
        
        // 4. Assert (Laptop)
        assertEquals(3, laptopAttrs.size());
        assertEquals("i7", laptopAttrs.get("cpu"));
        assertEquals("16", laptopAttrs.get("ram"));
        
        // 5. Assert (Mouse)
        assertEquals(2, mouseAttrs.size());
        assertEquals("800", mouseAttrs.get("dpi"));
        assertEquals("Wireless", mouseAttrs.get("connectionType"));
    }
}
