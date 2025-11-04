package Product;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import domain.entities.Keyboard;
import domain.entities.Laptop;
import domain.entities.Mouse;
import domain.entities.Product;

public class TestProductEntities {
	// Product failed
	@Test
    public void testProduct_ValidatePrice_Fail() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Product.validatePrice(-1);// Giá âm
        });
        assertEquals("Giá sản phẩm không được âm.", e.getMessage());
    }
	
	@Test
    public void testProduct_ValidateDescription_Fail_BadPrice() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Product.validateDescription("");
        });
        assertEquals("Mô tả sản phẩm không được để trống.", e.getMessage());
    }
	
	@Test
    public void testProduct_ValidateName_Fail() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Product.validateName("");
        });
        assertEquals("Tên sản phẩm không được để trống.", e.getMessage());
    }
	
	@Test
    public void testProduct_ValidateStock_Fail() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Product.validateStock(-1);
        });
        assertEquals("Số lượng tồn kho không được âm.", e.getMessage());
    }
	
	// product pass
	@Test
    public void testProduct_ValidatePrice_Success() {
        assertDoesNotThrow(() -> { // Khẳng định KHÔNG ném lỗi
            Product.validatePrice(1000);
        });
    }
	
	@Test
    public void testProduct_ValidateDescription_Success() {
        assertDoesNotThrow(() -> { // Khẳng định KHÔNG ném lỗi
            Product.validateDescription("Dễ dùng, bền");
        });
    }
	
	@Test
    public void testProduct_ValidateName_Success() {
        assertDoesNotThrow(() -> { // Khẳng định KHÔNG ném lỗi
            Product.validateName("Laptop Dell");
        });
    }
	
	@Test
    public void testProduct_ValidateStock_Success() {
        assertDoesNotThrow(() -> { // Khẳng định KHÔNG ném lỗi
            Product.validateStock(10);
        });
    }
    
	// Laptop failed
    @Test
    public void testLaptop_ValidateRam_Fail() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Laptop.validateRam(0); // Ram = 0
        });
        assertEquals("RAM phải là số dương.", e.getMessage());
    }
    
    @Test
    public void testLaptop_ValidateCpu_Fail_BadRam() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Laptop.validateCpu("");
        });
        assertEquals("CPU không được để trống.", e.getMessage());
    }
    
    @Test
    public void testLaptop_ValidateScreenSize_Fail() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Laptop.validateScreenSize("");
        });
        assertEquals("Kích thước màn hình không được để trống.", e.getMessage());
    }
    
    // Laptop passed
    @Test
    public void testLaptop_ValidateRam_Success() {
        assertDoesNotThrow(() -> { // Khẳng định KHÔNG ném lỗi
            Laptop.validateRam(8);
        });
    }
    
    @Test
    public void testLaptop_ValidateCpu_Success() {
        assertDoesNotThrow(() -> { // Khẳng định KHÔNG ném lỗi
            Laptop.validateCpu("I7");
        });
    }
    
    @Test
    public void testLaptop_ValidateScreenSize_Success() {
        assertDoesNotThrow(() -> { // Khẳng định KHÔNG ném lỗi
            Laptop.validateScreenSize("14 inch");
        });
    }
    
    // Mouse failed
    @Test
    public void testLaptop_ValidateDpi_Fail() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Mouse.validateDpi(-1); // Ram = 0
        });
        assertEquals("DPI phải là số dương.", e.getMessage());
    }
    
    @Test
    public void testLaptop_ValidateConnectionType_Fail() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Mouse.validateConnectionType(""); // Ram = 0
        });
        assertEquals("Loại connectionType không được để trống.", e.getMessage());
    }
    
    // Mouse passed
    @Test
    public void testMouse_ValidateDpi_Success() {
        assertDoesNotThrow(() -> { // Khẳng định KHÔNG ném lỗi
            Mouse.validateDpi(800);
        });
    }
    
    @Test
    public void testMouse_ValidateConnection_Success() {
        assertDoesNotThrow(() -> { // Khẳng định KHÔNG ném lỗi
            Mouse.validateConnectionType("Wireless");
        });
    }
    
    // Keyboard failed
    @Test
    public void testKeyboard_ValidateSwitchType_Fail() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Keyboard.validateSwitchType(""); 
        });
        assertEquals("Loại switch không được để trống.", e.getMessage());
    }
    
    @Test
    public void testKeyboard_ValidateLayout_Fail() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            Keyboard.validateLayout(""); 
        });
        assertEquals("Loại layout không được để trống.", e.getMessage());
    }
    
 // Keyboard passed
    @Test
    public void testKeyboard_ValidateSwitchType_Success() {
        assertDoesNotThrow(() -> { 
            Keyboard.validateSwitchType("Blue");
        });
    }
    
    @Test
    public void testKeyboard_ValidateLayout_Success() {
        assertDoesNotThrow(() -> { 
            Keyboard.validateLayout("Full Key");
        });
    }
}
